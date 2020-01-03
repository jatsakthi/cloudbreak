package com.sequenceiq.flow.reactor.config;

import com.sequenceiq.cloudbreak.logger.concurrent.MDCCleanerThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.bus.EventBus;
import reactor.core.Dispatcher;

import javax.inject.Inject;
import javax.inject.Named;

@Component
public class EventBusStatisticReporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusStatisticReporter.class);

    @Inject
    private EventBus eventBus;

    @Inject
    @Named("eventBusThreadPoolExecutor")
    private MDCCleanerThreadPoolExecutor executor;

    public void logInfoReport() {
        LOGGER.info("Reactor event bus statistics: {}", create());
    }

    public void logErrorReport() {
        LOGGER.error("Reactor state is critical, statistics: {}", create());
    }

    private EventBusStatistics create() {
        EventBusStatistics stats = new EventBusStatistics();
        Dispatcher dispatcher = eventBus.getDispatcher();
        stats.setBackLogSize(dispatcher.backlogSize());
        stats.setRemainingSlots(dispatcher.remainingSlots());
        stats.setInContext(dispatcher.inContext());

        stats.setPoolSize(executor.getPoolSize());
        stats.setCorePoolSize(executor.getCorePoolSize());
        stats.setActiveCount(executor.getActiveCount());
        stats.setTaskCount(executor.getTaskCount());
        stats.setActiveCount(executor.getActiveCount());
        stats.setCompletedTaskCount(executor.getCompletedTaskCount());

        return stats;
    }

    public static class EventBusStatistics {

        private long backLogSize;

        private long remainingSlots;

        private long getActiveCount;

        private long taskCount;

        private long completedTaskCount;

        private int corePoolSize;

        private int poolSize;

        private int activeCount;

        private boolean inContext;

        public void setGetActiveCount(long getActiveCount) {
            this.getActiveCount = getActiveCount;
        }

        public void setTaskCount(long taskCount) {
            this.taskCount = taskCount;
        }

        public void setCompletedTaskCount(long completedTaskCount) {
            this.completedTaskCount = completedTaskCount;
        }

        public void setCorePoolSize(int corePoolSize) {
            this.corePoolSize = corePoolSize;
        }

        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }

        public void setActiveCount(int activeCount) {
            this.activeCount = activeCount;
        }

        public void setBackLogSize(long backLogSize) {
            this.backLogSize = backLogSize;
        }

        public void setRemainingSlots(long remainingSlots) {
            this.remainingSlots = remainingSlots;
        }

        public void setInContext(boolean inContext) {
            this.inContext = inContext;
        }

        @Override
        public String toString() {
            return "EventBusStatistics{" +
                    "backLogSize=" + backLogSize +
                    ", remainingSlots=" + remainingSlots +
                    ", getActiveCount=" + getActiveCount +
                    ", taskCount=" + taskCount +
                    ", completedTaskCount=" + completedTaskCount +
                    ", corePoolSize=" + corePoolSize +
                    ", poolSize=" + poolSize +
                    ", activeCount=" + activeCount +
                    ", inContext=" + inContext +
                    '}';
        }
    }
}
