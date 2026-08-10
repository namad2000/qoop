package io.qoop.mapper.core.mapper;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;
import io.qoop.mapper.api.mapper.SourceMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PageMapper<S, T> extends SourceMapper<S, T> {

    // Converts Page (Target) to PageFilterData (Source)
    default PageFilterData<S> toPageFilterData(Page<T> page) {
        return PageFilterData.of(
                page.getTotalElements(),
                toSource(page.getContent())
        );
    }

    // Converts Page (Source) to PageData (Target)
    default PageData<S> toPageData(Page<T> page) {
        return PageData.of(
                page.getTotalElements(),
                page.getTotalPages(),
                toSource(page.getContent())
        );
    }

    // Converts PageFilterData (Source) to Page (Target)
    default Page<T> toPage(PageFilterData<S> pageFilterData) {
        return new PageImpl<>(
                toTarget(pageFilterData.getList()),
                Pageable.unpaged(),
                pageFilterData.getTotal()
        );
    }

    // Converts PageData (Target) to Page (Source)
    default Page<T> toPage(PageData<S> pageData) {
        return new PageImpl<>(
                toTarget(pageData.getContents()),
                Pageable.unpaged(),
                pageData.getTotalElements()
        );
    }
}
