package com.sequenceiq.flow.reactor;

import javax.inject.Inject;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;
import com.sequenceiq.flow.core.FlowConstants;
import com.sequenceiq.flow.core.TracingUtil;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import reactor.bus.Event;

@Component
@Aspect
public class FlowParametersAspects {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlowParametersAspects.class);

    @Inject
    private Tracer tracer;

    @Pointcut("execution(public * reactor.fn.Consumer+.accept(..)) && within(com.sequenceiq..*)")
    public void interceptReactorConsumersAcceptMethod() {
    }

    @Around("com.sequenceiq.flow.reactor.FlowParametersAspects.interceptReactorConsumersAcceptMethod()")
    public Object setFlowTriggerUserCrnForReactorHandler(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        Event<?> event = (Event<?>) proceedingJoinPoint.getArgs()[0];
        String flowTriggerUserCrn = event.getHeaders().get(FlowConstants.FLOW_TRIGGER_USERCRN);
        return ThreadBasedUserCrnProvider.doAsAndThrow(flowTriggerUserCrn, () -> {
            String operationName = event.getKey().toString();
            SpanContext spanContext = event.getHeaders().get(FlowConstants.SPAN_CONTEXT);
            Span activeSpan = tracer.activeSpan();
            if (TracingUtil.isActiveSpanReusable(activeSpan, spanContext, operationName)) {
                LOGGER.debug("Reusing existing span. {}", activeSpan.context());
                return doProceed(proceedingJoinPoint, flowTriggerUserCrn, event, spanContext);
            } else {
                Span span = TracingUtil.getSpan(tracer, operationName, spanContext, event.getHeaders().get(FlowConstants.FLOW_ID),
                        event.getHeaders().get(FlowConstants.FLOW_CHAIN_ID), flowTriggerUserCrn);
                LOGGER.debug("Creating new span. {}", span.context());
                try (Scope ignored = tracer.activateSpan(span)) {
                    if (spanContext == null) {
                        spanContext = span.context();
                    }
                    return doProceed(proceedingJoinPoint, flowTriggerUserCrn, event, spanContext);
                } finally {
                    span.finish();
                }
            }
        });
    }

    private Object doProceed(ProceedingJoinPoint proceedingJoinPoint, String flowTriggerUserCrn, Event<?> event, SpanContext spanContext) throws Throwable {
        event.getHeaders().set(FlowConstants.SPAN_CONTEXT, spanContext);
        if (flowTriggerUserCrn != null) {
            try {
                MDCBuilder.buildMdcContextFromCrn(Crn.fromString(flowTriggerUserCrn));
            } catch (Exception e) {
                LOGGER.debug("Couldn't set MDCContext from crn: [{}]", flowTriggerUserCrn, e);
            }
        }
        LOGGER.debug("A Reactor event handler's 'accept' method has been intercepted: {}, user crn on thread local is: {}",
                proceedingJoinPoint.toShortString(), flowTriggerUserCrn);
        return proceedingJoinPoint.proceed();
    }
}
