package io.qoop.mapper.core.mapper;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;
import io.qoop.mapper.api.mapper.BasicMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

public interface PageMapper<S, T> extends BasicMapper<S, T> {

    // Converts Page (Target) to PageFilterData (Source)
    default PageFilterData<T> toPageFilterData(Page<S> page) {
        return PageFilterData.of(
                page.getTotalElements(),
                toTarget(page.getContent())
        );
    }

    // Converts Page (Source) to PageData (Target)
    default PageData<T> toPageData(Page<S> page) {
        return PageData.of(
                page.getTotalElements(),
                page.getTotalPages(),
                toTarget(page.getContent())
        );
    }

    // Converts PageFilterData (Source) to Page (Target)
    default Page<S> toPage(PageFilterData<T> pageFilterData) {
        return new PageImpl<>(
                toSource(pageFilterData.getList()),
                Pageable.unpaged(),
                pageFilterData.getTotal()
        );
    }

    // Converts PageData (Target) to Page (Source)
    default Page<S> toPage(PageData<T> pageData) {
        return new PageImpl<>(
                toSource(pageData.getContents()),
                Pageable.unpaged(),
                pageData.getTotalElements()
        );
    }
}
