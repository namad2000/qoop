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
                    sorts.add(parseSortNode(node));
                }
            } else {
                sorts.add(parseSortNode(root));
            }
            wrapper.setSortSet(sorts);
            return wrapper;
        } catch (Exception e) {
            throw DomainException.of(SORT_INVALID_JSON, HttpStatus.BAD_REQUEST, source);
        }
    }

    private Sort parseSortNode(JsonNode node) {
        String property = null;
        Sort.Direction direction = null;

        if (node.has("property")) {
            property = node.get("property").asText();
        }
        if (node.has("direction")) {
            String dirStr = node.get("direction").asText();
            if (dirStr != null) {
                for (Sort.Direction d : Sort.Direction.values()) {
                    if (d.name().equalsIgnoreCase(dirStr) || (d.getName() != null && d.getName().equalsIgnoreCase(dirStr))) {
                        direction = d;
                        break;
                    }
                }
            }
        }

        return new Sort(direction, property);
    }
}