package io.qoop.mapper.api.mapper;

import io.qoop.domain.model.PageData;
import io.qoop.domain.model.PageFilterData;

public interface DomainPageMapper<S, T> extends TargetMapper<S, T> {

    default PageFilterData<T> toPageFilterData(PageFilterData<S> pageFilterData) {
        return PageFilterData.of(
                pageFilterData.getTotal(),
                toTarget(pageFilterData.getList())
        );
    }

    default PageData<T> toPageData(PageData<S> pageData) {
        return PageData.of(
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                toTarget(pageData.getContents())
        );
    }
}
