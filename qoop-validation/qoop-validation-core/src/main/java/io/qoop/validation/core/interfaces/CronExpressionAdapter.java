package io.qoop.validation.core.interfaces;

import io.qoop.validation.api.interfaces.CronExpression;
import org.springframework.stereotype.Component;

/**
 * Domain component for validating Quartz/Spring standard cron expressions.
 *
 * @author Davood Akbari - 1404
 */

@Component
public class CronExpressionAdapter implements CronExpression {

    @Override
    public boolean isValidExpression(String cron) {
        if (cron == null || cron.trim().isEmpty()) {
            return false;
        }

        return org.springframework.scheduling.support.CronExpression.isValidExpression(cron.trim());
    }
}