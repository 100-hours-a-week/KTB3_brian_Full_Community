package com.community.global.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompositePermissionEvaluator implements PermissionEvaluator {

    private final Map<String, TargetAwarePermissionEvaluator> targetTypeDelegates = new HashMap<>();

    public CompositePermissionEvaluator(List<TargetAwarePermissionEvaluator> evaluators) {
        for (TargetAwarePermissionEvaluator evaluator : evaluators) {
            targetTypeDelegates.put(evaluator.supportType(), evaluator);
        }
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        PermissionEvaluator delegate = targetTypeDelegates.get(targetType);
        return delegate != null && delegate.hasPermission(auth, targetId, targetType, permission);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false;
    }
}
