package io.qoop.jpa.persistence.specification;


import io.qoop.builder.specification.api.model.Filter;
import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.builder.specification.api.model.SortWrapper;
import io.qoop.utils.api.enums.EnumHelper;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.ArrayList;
import java.util.List;

public interface JpaSpecificationBuilder<T, ID> extends JpaSpecificationExecutor<T> {

    default Page<T> findAll(FilterWrapper filterWrapper,
                            SortWrapper sortWrapper,
                            Integer start,
                            Integer limit) {

        if (start == null || start < 0) {
            start = 0;
        }
        if (limit == null || limit <= 0) {
            limit = 100;
        }

        Specification<T> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filterWrapper != null && filterWrapper.getFilters() != null) {
                for (Filter filter : filterWrapper.getFilters()) {
                    Path<?> path = getPath(root, filter.getProperty());
                    Class<?> fieldClass = path.getJavaType();

                    switch (filter.getOperator()) {
                        case EQUAL, _EQUAL -> handleEqualOperator(cb, path, fieldClass, filter.getValue(), predicates);
                        case NOT_EQUAL -> handleNotEqualOperator(cb, path, fieldClass, filter.getValue(), predicates);
                        case IN -> predicates.add(path.in(filter.getValue().split(",")));
                        case NOT_IN -> predicates.add(path.in(filter.getValue().split(",")).not());
                        case LIKE ->
                                predicates.add(cb.like(path.as(String.class), "%" + filter.getValue().replace("*", "") + "%"));
                        case BETWEEN -> handleBetweenOperator(cb, path, filter.getValue(), predicates);
                        case GREATER_THAN_OR_EQUAL ->
                                predicates.add(cb.greaterThanOrEqualTo(path.as(String.class), filter.getValue()));
                        case LESS_THAN_OR_EQUAL ->
                                predicates.add(cb.lessThanOrEqualTo(path.as(String.class), filter.getValue()));
                        case GREATER_THAN -> predicates.add(cb.greaterThan(path.as(String.class), filter.getValue()));
                        case LESS_THAN -> predicates.add(cb.lessThan(path.as(String.class), filter.getValue()));
                        case IS_NULL -> predicates.add(cb.isNull(path));
                        case IS_NOT_NULL -> predicates.add(cb.isNotNull(path));
                        default -> throw new IllegalArgumentException("Unsupported operation: " + filter.getOperator());
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (sortWrapper != null && sortWrapper.getSortSet() != null) {
            sort = sortWrapper.getSortSet().stream()
                    .map(s -> s.getDirection() == io.qoop.builder.specification.api.model.Sort.Direction.ASC
                            ? Sort.by(s.getProperty()).ascending()
                            : Sort.by(s.getProperty()).descending())
                    .reduce(Sort::and)
                    .orElse(Sort.unsorted());
        }

        Pageable pageable = PageRequest.of(start / limit, limit, sort);
        return this.findAll(spec, pageable);
    }

    private Path<?> getPath(Path<?> root, String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        Path<?> path = root;
        for (String part : parts) {
            path = path.get(part);
        }
        return path;
    }

    private void handleEqualOperator(CriteriaBuilder cb, Path<?> path, Class<?> fieldClass, Object value, List<Predicate> predicates) {
        if (fieldClass.isEnum()) {
            Object enumValue = EnumHelper.getEnumValueFromString(fieldClass, (String) value);
            predicates.add(cb.equal(path, enumValue));
        } else if (Boolean.class.equals(fieldClass)) {
            Boolean booleanValue = value.equals("true") ? Boolean.TRUE : Boolean.FALSE;
            predicates.add(cb.equal(path, booleanValue));
        } else {
            predicates.add(cb.equal(path, value));
        }
    }

    private void handleNotEqualOperator(CriteriaBuilder cb, Path<?> path, Class<?> fieldClass, Object value, List<Predicate> predicates) {
        if (fieldClass.isEnum()) {
            Object enumValue = EnumHelper.getEnumValueFromString(fieldClass, (String) value);
            predicates.add(cb.notEqual(path, enumValue));
        } else if (Boolean.class.equals(fieldClass)) {
            Boolean booleanValue = value.equals("true") ? Boolean.TRUE : Boolean.FALSE;
            predicates.add(cb.notEqual(path, booleanValue));
        } else {
            predicates.add(cb.notEqual(path, value));
        }
    }

    private void handleBetweenOperator(CriteriaBuilder cb, Path<?> path, String value, List<Predicate> predicates) {
        String[] values = value.split(",");
        if (values.length == 2) {
            predicates.add(cb.between(path.as(String.class), values[0], values[1]));
        }
    }
}