package io.qoop.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class PageFilterData<T> {

    private final long total;
    private final List<T> list;

    public static <T> PageFilterData<T> of(long total, List<T> list) {
        return new PageFilterData<>(total, list);
    }
}
