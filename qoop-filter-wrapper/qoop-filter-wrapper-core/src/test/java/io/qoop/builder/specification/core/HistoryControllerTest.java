package io.qoop.builder.specification.core;

import io.qoop.builder.specification.api.model.FilterWrapper;
import io.qoop.builder.specification.api.model.SortWrapper;
import io.qoop.builder.specification.core.config.FilterConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = HistoryController.class)
@Import({FilterWrapperConverter.class, SortWrapperConverter.class})
@ContextConfiguration(classes = {
        HistoryController.class,
        FilterWrapperConverter.class,
        SortWrapperConverter.class,
        FilterConfig.class
})
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @Test
    void shouldConvertFilterAndPassItToService() throws Exception {

        String jsonFilter = """
                {
                  "property": "name",
                  "value": "davood",
                  "operator": "EQUAL"
                }
                """;

        when(historyService.findAll(any(), anyInt(), anyInt()))
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(
                        get("/history")
                                .param("filter", jsonFilter)
                                .param("start", "5")
                                .param("limit", "20")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<FilterWrapper> captor = ArgumentCaptor.forClass(FilterWrapper.class);

        verify(historyService, times(1))
                .findAll(captor.capture(), eq(5), eq(20));

        FilterWrapper passedFilter = captor.getValue();

        assertThat(passedFilter).isNotNull();
        assertThat(passedFilter.getFilters()).hasSize(1);
        assertThat(passedFilter.getBinaryFilters()).isEmpty();

        var filter = passedFilter.getFilters().iterator().next();
        assertThat(filter.getProperty()).isEqualTo("name");
        assertThat(filter.getValue()).isEqualTo("davood");
    }

    @Test
    void shouldHandleMissingFilterParameter() throws Exception {

        when(historyService.findAll(any(), anyInt(), anyInt()))
                .thenReturn(java.util.Collections.emptyList());

        mockMvc.perform(get("/history"))
                .andExpect(status().isOk());

        verify(historyService).findAll(isNull(), eq(0), eq(10));
    }

    @Test
    void shouldReturnBadRequestWhenFilterIsInvalidJson() throws Exception {

        String invalidJson = "{ invalid json";

        mockMvc.perform(
                        get("/history")
                                .param("filter", invalidJson)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldConvertSortAndPassItToServiceInSearch() throws Exception {
        String jsonSort = """
                {
                    "direction": "desc",
                    "property": "age"
                }
                """;

        when(historyService.findAll(any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(
                        get("/history/search")
                                .param("sorts", jsonSort)
                                .param("start", "0")
                                .param("limit", "10")
                )
                .andExpect(status().isOk());

        ArgumentCaptor<SortWrapper> sortCaptor = ArgumentCaptor.forClass(SortWrapper.class);
        verify(historyService, times(1))
                .findAll(any(), sortCaptor.capture(), eq(0), eq(10));

        SortWrapper passedSort = sortCaptor.getValue();
        assertThat(passedSort).isNotNull();
        assertThat(passedSort.getSortSet()).hasSize(1);
        var sort = passedSort.getSortSet().iterator().next();
        assertThat(sort.getProperty()).isEqualTo("age");
        assertThat(sort.getDirection()).isEqualTo(io.qoop.builder.specification.api.model.Sort.Direction.DESC);
    }
}