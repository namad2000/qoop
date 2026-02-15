package io.qoop.filter.bean.core;

import io.qoop.filter.bean.api.DomainMapper;
import io.qoop.filter.bean.api.DomainService;
import io.qoop.filter.bean.api.DomainValidator;
import io.qoop.filter.bean.api.UseCaseService;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * @author Davood Akbari - 1404
 * daak1365@gmail.com
 * daak1365@yahoo.com
 * 09125188694
 */

@Configuration
@ComponentScan(
        basePackages = {"${app.scan.packages}"},
        includeFilters = {
                @ComponentScan.Filter(
                        type = FilterType.ANNOTATION,
                        classes = {
                                DomainValidator.class,
                                DomainMapper.class,
                                DomainService.class,
                                UseCaseService.class
                        }
                )
        }
)
public class BeanFilterConfiguration {
}
