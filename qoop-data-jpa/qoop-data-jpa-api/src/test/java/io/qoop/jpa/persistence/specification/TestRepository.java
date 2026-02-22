package io.qoop.jpa.persistence.specification;


import org.springframework.data.jpa.repository.JpaRepository;

public interface TestRepository extends
        JpaRepository<TestEntity, Long>,
        JpaSpecificationBuilder<TestEntity, Long> {
}