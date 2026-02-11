package io.qoop.property.core;

import io.qoop.property.api.Property;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

@Component
public class PropertyInjector implements BeanPostProcessor {

    private final Environment environment;
    private final ConversionService conversionService;

    public PropertyInjector(Environment environment, ConfigurableBeanFactory beanFactory) {
        this.environment = environment;
        this.conversionService = beanFactory.getConversionService();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Property.class)) {
                Property annotation = field.getAnnotation(Property.class);
                String rawValue = environment.getProperty(annotation.key(), annotation.defaultValue());

                injectValue(bean, field, rawValue, beanName);
            }
        }
        return bean;
    }

    private void injectValue(Object bean, Field field, String value, String beanName) {
        boolean accessible = field.canAccess(bean);
        field.setAccessible(true);
        try {
            Object convertedValue = conversionService.convert(value, field.getType());
            field.set(bean, convertedValue);
        } catch (Exception e) {
            throw new BeanInitializationException(
                    String.format("Failed to set property '%s' on bean '%s'. Could not convert value [%s] to type [%s]",
                            field.getName(), beanName, value, field.getType().getSimpleName()), e);
        } finally {
            field.setAccessible(accessible);
        }
    }
}
