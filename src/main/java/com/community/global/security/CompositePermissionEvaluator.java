package com.community.global.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CompositePermissionEvaluator implements PermissionEvaluator {

    private final Map<String, TargetAwarePermissionEvaluator> delegates = new HashMap<>();

    public CompositePermissionEvaluator(List<TargetAwarePermissionEvaluator> evaluators) {
        for (TargetAwarePermissionEvaluator evaluator : evaluators) {
            for (String type : Arrays.stream(TARGET_TYPES.values()).map(Enum::name).toList() ) {
                if (evaluator.supports(type)) {
                    delegates.put(type, evaluator);
                }
            }
        }
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        PermissionEvaluator delegate = delegates.get(targetType);
        return delegate != null && delegate.hasPermission(auth, targetId, targetType, permission);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false;
    }
}
