package io.qoop.builder.specification.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qoop.builder.specification.api.model.Sort;
import io.qoop.builder.specification.api.model.SortWrapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SortWrapperConverterTest {
    private final SortWrapperConverter converter = new SortWrapperConverter(new ObjectMapper());

    @Test
    void shouldConvertSingleSort() {
        String json = """
                {
                    "direction": "asc",
                    "property": "name"
                }
                """;
        SortWrapper wrapper = converter.convert(json);
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getSortSet()).hasSize(1);
        Sort sort = wrapper.getSortSet().iterator().next();
        assertThat(sort.getProperty()).isEqualTo("name");
        assertThat(sort.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void shouldConvertSortArray() {
        String json = """
                [
                    {"direction": "asc","property": "name"},
                    {"direction": "desc","property": "age"}
                ]
                """;
        SortWrapper wrapper = converter.convert(json);
        assertThat(wrapper).isNotNull();
        assertThat(wrapper.getSortSet()).hasSize(2);
    }

    @Test
    void shouldReturnEmptyWrapperWhenNullOrEmpty() {
        assertThat(converter.convert(null).getSortSet()).isEmpty();
        assertThat(converter.convert("").getSortSet()).isEmpty();
        assertThat(converter.convert("null").getSortSet()).isEmpty();
    }
}