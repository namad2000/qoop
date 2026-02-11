package io.qoop.message.api.core;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Author: davood akbari
 * Email: daak1365@gmail.com
 * Created: 12/29/2025 11:09 AM
 * Package: io.qoop
 */

@Configuration
@PropertySource(value = "classpath:message.yml", factory = YamlPropertySourceFactory.class)
public class I18nConfig {

    @Bean
    @Primary
    public MessageSource messagesMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames(
                "classpath:i18n/messages/messages",
                "classpath*:i18n/messages/messages"
        );
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }

    @Bean
    @Qualifier("fieldsMessageSource")
    public MessageSource fieldsMessageSource() {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();
        ms.setBasenames(
                "classpath:i18n/fields/fields",
                "classpath*:i18n/fields/fields"
        );
        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        return ms;
    }
}
