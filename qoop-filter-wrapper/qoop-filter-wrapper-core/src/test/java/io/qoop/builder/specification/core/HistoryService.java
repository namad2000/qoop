package io.qoop.builder.specification.core;

import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.builder.specification.api.model.SortWrapper;

import java.util.List;

public interface HistoryService {

    List<?> findAll(FilterWrapper filter, Integer start, Integer limit);

    List<?> findAll(FilterWrapper filter, SortWrapper sorts, Integer start, Integer limit);
}
