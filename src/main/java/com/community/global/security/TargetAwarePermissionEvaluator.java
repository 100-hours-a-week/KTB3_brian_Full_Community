package com.community.global.security;

import org.springframework.security.access.PermissionEvaluator;

public interface TargetAwarePermissionEvaluator extends PermissionEvaluator {

    String supportType();
}
