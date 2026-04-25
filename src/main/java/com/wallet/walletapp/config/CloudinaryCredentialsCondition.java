package com.wallet.walletapp.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

public class CloudinaryCredentialsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String cloudName = context.getEnvironment().getProperty("cloudinary.cloud-name");
        String apiKey = context.getEnvironment().getProperty("cloudinary.api-key");
        String apiSecret = context.getEnvironment().getProperty("cloudinary.api-secret");

        return StringUtils.hasText(cloudName)
                && StringUtils.hasText(apiKey)
                && StringUtils.hasText(apiSecret);
    }
}
