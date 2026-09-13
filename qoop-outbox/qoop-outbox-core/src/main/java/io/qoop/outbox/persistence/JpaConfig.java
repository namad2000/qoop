package io.qoop.outbox.persistence;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories("io.qoop.outbox.persistence.repository")
@EntityScan("io.qoop.outbox.persistence.entity")
public class JpaConfig {
}
