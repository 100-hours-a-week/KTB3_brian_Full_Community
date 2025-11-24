package com.community.global.security;

import org.springframework.security.access.PermissionEvaluator;

public interface TargetAwarePermissionEvaluator extends PermissionEvaluator {

    boolean supports(String targetType);
}
