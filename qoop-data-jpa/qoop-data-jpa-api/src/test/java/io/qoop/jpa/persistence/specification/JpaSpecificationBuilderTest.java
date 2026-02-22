package io.qoop.jpa.persistence.specification;

import io.qoop.builder.specification.api.model.Filter;
import io.qoop.builder.specification.api.model.FilterWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestJpaConfig.class)
@EnableJpaRepositories(basePackages = "io.qoop.jpa.persistence.specification")
@EntityScan(basePackages = "io.qoop.jpa.persistence.specification")
@Transactional
class JpaSpecificationBuilderTest {

    @Autowired
    private TestRepository repository;

    @BeforeEach
    void setUp() {
        repository.save(TestEntity.builder().name("Ali").age(25).active(true).build());
        repository.save(TestEntity.builder().name("Reza").age(30).active(false).build());
    }

    @Test
    void shouldFilterByEqual() {

        Filter filter = new Filter();
        filter.setProperty("name");
        filter.setOperator(Filter.Operator.EQUAL);
        filter.setValue("Ali");

        FilterWrapper wrapper = new FilterWrapper();
        wrapper.addFilter(filter);

        Page<TestEntity> result = repository.findAll(wrapper, null, 0, 10);

        assertThat(result.getTotalElements()).isEqualTo(1);
    }
}