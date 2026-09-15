package io.qoop.transaction.core.config;

import io.qoop.transaction.core.DomainTransactionAspect;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Auto-configuration class for Domain Transaction Management using @DomainTransactional.
 */
@Configuration
@AutoConfiguration
@EnableAspectJAutoProxy
@Import(DomainTransactionAspect.class)
public class DomainTransactionAutoConfiguration {

    @Primary
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

