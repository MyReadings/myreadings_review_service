package org.modular.playground.review.core.domain;

import java.util.List;

public record PageResult<T>(List<T> content, boolean hasMore) {
    public static <T> PageResult<T> from(List<T> rows, int size) {
        return new PageResult<>(List.copyOf(rows.subList(0, Math.min(size, rows.size()))), rows.size() > size);
    }
}
