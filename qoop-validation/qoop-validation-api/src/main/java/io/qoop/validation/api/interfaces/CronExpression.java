package io.qoop.validation.api.interfaces;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 8/27/2026 4:31 PM
 * Package: io.qoop.validation.api.validator
 */

public interface CronExpression {
    boolean isValidExpression(String cron);
}
