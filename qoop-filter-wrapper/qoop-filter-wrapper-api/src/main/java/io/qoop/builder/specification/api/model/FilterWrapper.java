package io.qoop.builder.specification.api.model;


import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class FilterWrapper {
    private Set<Filter> filters = new HashSet<>();
    private Set<BinaryFilter> binaryFilters = new HashSet<>();

    public void addFilter(Filter f) {
        this.filters.add(f);
    }

    public void addBinaryFilter(BinaryFilter bf) {
        this.binaryFilters.add(bf);
    }
}