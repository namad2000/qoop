package io.qoop.builder.specification.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.Sort;
import io.qoop.builder.specification.api.model.SortWrapper;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.presentation.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

import static io.qoop.builder.specification.api.model.SpecificationExceptionCode.SORT_INVALID_JSON;

@Component
@RequiredArgsConstructor
public class SortWrapperConverter implements Converter<String, SortWrapper> {

    private final ObjectMapper mapper;

    @Override
    public SortWrapper convert(String source) {
        if (source == null || source.isBlank() || "null".equalsIgnoreCase(source)) {
            return new SortWrapper();
        }

        try {
            SortWrapper wrapper = new SortWrapper();
            JsonNode root = mapper.readTree(source);
            Set<Sort> sorts = new LinkedHashSet<>();

            if (root.isArray()) {
                for (JsonNode node : root) {
                    sorts.add(mapper.convertValue(node, Sort.class));
                }
            } else {
                sorts.add(mapper.convertValue(root, Sort.class));
            }

            wrapper.setSortSet(sorts);
            return wrapper;
        } catch (Exception e) {
            throw DomainException.of(SORT_INVALID_JSON, HttpStatus.BAD_REQUEST, source);
        }
    }
}