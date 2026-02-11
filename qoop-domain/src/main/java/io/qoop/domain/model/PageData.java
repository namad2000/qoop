package io.qoop.domain.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class PageData<T> {

    private final long totalElements;
    private final int totalPages;
    private final List<T> contents;

    public static <T> PageData<T> of(long totalElements, int totalPages, List<T> contents) {
        return new PageData<>(totalElements, totalPages, contents);
    }
}
