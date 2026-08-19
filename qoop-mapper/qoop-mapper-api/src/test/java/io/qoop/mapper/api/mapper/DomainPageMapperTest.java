package io.qoop.mapper.api.mapper;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainPageMapperTest {

    record SourceItem(String name) {}
    record TargetItem(String name) {}

    static class ItemPageMapper implements DomainPageMapper<SourceItem, TargetItem> {
        @Override
        public TargetItem toTarget(SourceItem sourceItem) {
            return new TargetItem(sourceItem.name());
        }
    }

    private final DomainPageMapper<SourceItem, TargetItem> mapper = new ItemPageMapper();

    @Test
    void testToPageFilterData() {
        PageFilterData<SourceItem> sourcePageFilter = PageFilterData.of(
                100L, 
                List.of(new SourceItem("Item1"), new SourceItem("Item2"))
        );

        PageFilterData<TargetItem> result = mapper.toPageFilterData(sourcePageFilter);

        assertNotNull(result);
        assertEquals(100L, result.getTotal());
        assertEquals(2, result.getList().size());
        assertEquals("Item1", result.getList().get(0).name());
        assertEquals("Item2", result.getList().get(1).name());
    }

    @Test
    void testToPageData() {
        PageData<SourceItem> sourcePageData = PageData.of(
                50L,
                5,
                List.of(new SourceItem("Alpha"), new SourceItem("Beta"))
        );

        PageData<TargetItem> result = mapper.toPageData(sourcePageData);

        assertNotNull(result);
        assertEquals(50L, result.getTotalElements());
        assertEquals(5, result.getTotalPages());
        assertEquals(2, result.getContents().size());
        assertEquals("Alpha", result.getContents().get(0).name());
        assertEquals("Beta", result.getContents().get(1).name());
    }
}