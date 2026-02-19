package io.qoop.builder.specification.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.BinaryFilter;
import io.qoop.builder.specification.api.model.Filter;
import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.fault.handler.api.exception.DomainException;
import io.qoop.fault.handler.api.presentation.HttpStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static io.qoop.builder.specification.api.model.SpecificationExceptionCode.FILTER_INVALID_JSON;

@Component
@RequiredArgsConstructor
public class FilterWrapperConverter implements Converter<String, FilterWrapper> {

    private final ObjectMapper mapper;

    @Override
    public FilterWrapper convert(String source) {
        if (source == null || source.isEmpty() || "null".equals(source)) {
            return new FilterWrapper();
        }

        try {
            FilterWrapper wrapper = new FilterWrapper();
            JsonNode root = mapper.readTree(source);

            if (root.isArray()) {
                for (JsonNode node : root) {
                    processNode(node, wrapper);
                }
            } else {
                processNode(root, wrapper);
            }
            return wrapper;
        } catch (Exception e) {
            throw DomainException.of(FILTER_INVALID_JSON, HttpStatus.BAD_REQUEST, source);
        }
    }

    private void processNode(JsonNode node, FilterWrapper wrapper) {
        if (node.has("first")) {
            wrapper.addBinaryFilter(mapper.convertValue(node, BinaryFilter.class));
        } else {
            wrapper.addFilter(mapper.convertValue(node, Filter.class));
        }
    }
}