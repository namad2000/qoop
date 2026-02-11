package io.qoop.filter.bean.core;


import io.qoop.filter.bean.api.DomainMapper;

// Example mapper annotated with your custom annotation
@DomainMapper
public class SomeDomainMapper {

    public String mapEntityToDto(String entity) {
        // Simple mapping example
        return "DTO:" + entity;
    }
}
