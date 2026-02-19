package io.qoop.builder.specification.api.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;


@Data
@NoArgsConstructor
public class SortWrapper {

    private Set<Sort> sortSet = new LinkedHashSet<>();
}