package io.qoop.builder.specification.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.BinaryFilter;
import io.qoop.builder.specification.api.model.Filter;
import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.fault.handler.api.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FilterWrapperConverterTest {

    private FilterWrapperConverter converter;

    @BeforeEach
    void setUp() {
        converter = new FilterWrapperConverter(new ObjectMapper());
    }

    @Test
    void shouldReturnEmptyWrapperWhenSourceIsNull() {
        FilterWrapper result = converter.convert(null);

        assertThat(result).isNotNull();
        assertThat(result.getFilters()).isEmpty();
        assertThat(result.getBinaryFilters()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWrapperWhenSourceIsEmpty() {
        FilterWrapper result = converter.convert("");

        assertThat(result).isNotNull();
        assertThat(result.getFilters()).isEmpty();
        assertThat(result.getBinaryFilters()).isEmpty();
    }

    @Test
    void shouldReturnEmptyWrapperWhenSourceIsStringNull() {
        FilterWrapper result = converter.convert("null");

        assertThat(result).isNotNull();
        assertThat(result.getFilters()).isEmpty();
        assertThat(result.getBinaryFilters()).isEmpty();
    }

    @Test
    void shouldConvertSingleFilterObject() {
        String json = """
                {
                  "property": "name",
                  "value": "davood",
                  "operator": "EQUAL"
                }
                """;

        FilterWrapper result = converter.convert(json);

        assertThat(result.getFilters()).hasSize(1);
        assertThat(result.getBinaryFilters()).isEmpty();

        Filter filter = result.getFilters().iterator().next();
        assertThat(filter.getProperty()).isEqualTo("name");
        assertThat(filter.getValue()).isEqualTo("davood");
        assertThat(filter.getOperator()).isEqualTo(Filter.Operator.EQUAL);
    }

    @Test
    void shouldConvertArrayOfFilters() {
        String json = """
                [
                  {
                    "property": "name",
                    "value": "davood",
                    "operator": "EQUAL"
                  },
                  {
                    "property": "age",
                    "value": "30",
                    "operator": "GREATER_THAN"
                  }
                ]
                """;

        FilterWrapper result = converter.convert(json);

        assertThat(result.getFilters()).hasSize(2);
        assertThat(result.getBinaryFilters()).isEmpty();
    }

    @Test
    void shouldConvertBinaryFilter() {
        String json = """
                {
                  "first": {
                    "property": "age",
                    "value": "20",
                    "operator": "GREATER_THAN"
                  },
                  "second": {
                    "property": "age",
                    "value": "30",
                    "operator": "LESS_THAN"
                  }
                }
                """;

        FilterWrapper result = converter.convert(json);

        assertThat(result.getBinaryFilters()).hasSize(1);
        assertThat(result.getFilters()).isEmpty();

        BinaryFilter bf = result.getBinaryFilters().iterator().next();
        assertThat(bf.getFirst().getOperator()).isEqualTo(Filter.Operator.GREATER_THAN);
        assertThat(bf.getSecond().getOperator()).isEqualTo(Filter.Operator.LESS_THAN);
    }

    @Test
    void shouldThrowDomainExceptionWhenJsonIsInvalid() {
        String invalidJson = "{ invalid json";

        assertThatThrownBy(() -> converter.convert(invalidJson))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("FILTER_INVALID_01");
    }
}
