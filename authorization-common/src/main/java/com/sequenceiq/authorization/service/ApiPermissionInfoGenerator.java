package com.sequenceiq.authorization.service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MemberUsageScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.scanners.MethodParameterScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.sequenceiq.authorization.annotation.AuthorizationResource;
import com.sequenceiq.authorization.annotation.CheckPermissionByAccount;
import com.sequenceiq.authorization.annotation.CheckPermissionByEnvironmentCrn;
import com.sequenceiq.authorization.annotation.CheckPermissionByEnvironmentName;
import com.sequenceiq.authorization.annotation.CheckPermissionByResourceCrn;
import com.sequenceiq.authorization.annotation.CheckPermissionByResourceCrnList;
import com.sequenceiq.authorization.annotation.CheckPermissionByResourceName;
import com.sequenceiq.authorization.annotation.CheckPermissionByResourceNameList;
import com.sequenceiq.authorization.annotation.CheckPermissionByResourceObject;
import com.sequenceiq.authorization.annotation.DisableCheckPermissions;
import com.sequenceiq.authorization.annotation.FilterListBasedOnPermissions;

@Component
public class ApiPermissionInfoGenerator implements InfoContributor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiPermissionInfoGenerator.class);

    private static final Reflections REFLECTIONS = new Reflections("com.sequenceiq",
            new FieldAnnotationsScanner(),
            new TypeAnnotationsScanner(),
            new SubTypesScanner(false),
            new MemberUsageScanner(),
            new MethodAnnotationsScanner(),
            new MethodParameterScanner(),
            new MethodParameterNamesScanner());

    @Inject
    private UmsRightProvider umsRightProvider;


    @Override
    public void contribute(Info.Builder builder) {
        Map<String, Object> permissionMap = Maps.newHashMap();
        permissionMap.put("requiredRightsForApis", generateApiMethodsWithRequiredPermission());
        builder.withDetails(permissionMap);
    }

    public Map<String, Object> generateApiMethodsWithRequiredPermission() {
        Set<Class<?>> authorizationResourceClasses = REFLECTIONS.getTypesAnnotatedWith(AuthorizationResource.class);
        Map<String, Object> methodMap = Maps.newHashMap();
        authorizationResourceClasses.stream().forEach(authzClass -> methodMap.putAll(getMethodsForClass(authzClass, getPath(authzClass))));
        return methodMap;
    }

    private Map<String, Object> getMethodsForClass(Class<?> authzClass, String apiPath) {
        Class[] interfaces = authzClass.getInterfaces();
        Map<String, Object> result = Maps.newHashMap();
        Arrays.stream(interfaces).forEach(controllerInterface -> {
            Annotation[] declaredAnnotations = controllerInterface.getAnnotations();
            Optional<Annotation> interfaceWithPath = Arrays.stream(declaredAnnotations).filter(annotation ->
                    annotation.annotationType().equals(Path.class)).findFirst();
            if (interfaceWithPath.isPresent()) {
                Arrays.stream(controllerInterface.getMethods()).forEach(method -> {
                    Optional<Annotation> methodWithPath = Arrays.stream(method.getAnnotations()).filter(annotation ->
                            annotation.annotationType().equals(Path.class)).findFirst();
                    if (methodWithPath.isPresent()) {
                        String methodPath = ((Path) methodWithPath.get()).value();
                        if (!methodPath.startsWith("/")) {
                            methodPath = "/" + methodPath;
                        }
                        result.put(apiPath + methodPath + " " + getMethodType(method), getRight(controllerInterface, method));
                    }
                });
            }
        });
        return result;
    }

    private String getRight(Class apiInterface, Method method) {
        AtomicReference<String> result = new AtomicReference<>("right_notfound");
        Set<Class> subTypesOf = REFLECTIONS.getSubTypesOf(apiInterface);
        Arrays.stream(subTypesOf.iterator().next().getMethods()).forEach(controllerMethod -> {
            if (StringUtils.equals(controllerMethod.getName(), method.getName())) {
                result.set(getAuthorizationRight(controllerMethod));
            }
        });
        return result.get();
    }

    private static String getMethodType(Method method) {
        if (method.isAnnotationPresent(GET.class)) {
            return "GET";
        }
        if (method.isAnnotationPresent(DELETE.class)) {
            return "DELETE";
        }
        if (method.isAnnotationPresent(PUT.class)) {
            return "PUT";
        }
        if (method.isAnnotationPresent(POST.class)) {
            return "POST";
        }
        return "";
    }

    private String getAuthorizationRight(Method method) {
        if (method.isAnnotationPresent(CheckPermissionByResourceCrn.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByResourceCrn.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByAccount.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByAccount.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByResourceObject.class)) {
            return "TODO";
        }
        if (method.isAnnotationPresent(CheckPermissionByResourceName.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByResourceName.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByResourceCrnList.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByResourceCrnList.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByResourceNameList.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByResourceNameList.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByEnvironmentName.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByEnvironmentName.class).action());
        }
        if (method.isAnnotationPresent(CheckPermissionByEnvironmentCrn.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(CheckPermissionByEnvironmentCrn.class).action());
        }
        if (method.isAnnotationPresent(FilterListBasedOnPermissions.class)) {
            return umsRightProvider.getNewRight(method.getAnnotation(FilterListBasedOnPermissions.class).action());
        }
        if (method.isAnnotationPresent(DisableCheckPermissions.class)) {
            return "no_permission_needed";
        }
        return "";
    }

    private String getPath(Class authzClass) {
        AtomicReference<String> path = new AtomicReference<>("notvalid");
        Class[] interfaces = authzClass.getInterfaces();
        Arrays.stream(interfaces).forEach(controllerInterface -> {
            Annotation[] declaredAnnotations = controllerInterface.getAnnotations();
            Optional<Annotation> interfaceWithPath = Arrays.stream(declaredAnnotations).filter(annotation ->
                    annotation.annotationType().equals(Path.class)).findFirst();
            if (interfaceWithPath.isPresent()) {
                path.set(((Path) interfaceWithPath.get()).value());
            }
        });
        return path.get();
    }
}
