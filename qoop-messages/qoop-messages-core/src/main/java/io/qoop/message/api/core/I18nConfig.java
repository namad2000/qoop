package io.qoop.message.api.core;

import io.qoop.properties.factory.YamlPropertySourceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

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
    public MessageSource messagesMessageSource() throws IOException {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();

        String[] basenames = resolveBaseNames("classpath*:i18n/messages/*messages*.properties");
        ms.setBasenames(basenames);

        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(Locale.of("fa", "IR"));
        return ms;
    }

    @Bean
    @Qualifier("fieldsMessageSource")
    public MessageSource fieldsMessageSource() throws IOException {
        ReloadableResourceBundleMessageSource ms = new ReloadableResourceBundleMessageSource();

        String[] basenames = resolveBaseNames("classpath*:i18n/fields/*fields*.properties");
        ms.setBasenames(basenames);

        ms.setDefaultEncoding("UTF-8");
        ms.setFallbackToSystemLocale(false);
        ms.setDefaultLocale(Locale.of("fa", "IR"));

        return ms;
    }

    private String[] resolveBaseNames(String pattern) throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources(pattern);

        return Arrays.stream(resources)
                .map(resource -> {
                    try {
                        String uri = resource.getURI().toString();
                        String relativePath = uri.substring(uri.indexOf("i18n/"));
                        String basename = relativePath.replaceAll("(_[a-z]{2}(_[A-Z]{2})?)?\\.properties$", "");
                        return "classpath:" + basename;
                    } catch (IOException e) {
                        throw new RuntimeException("Error resolving message source resource", e);
                    }
                })
                .distinct()
                .toArray(String[]::new);
    }
}