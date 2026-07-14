package com.architek.oikos.shared.domain.pagination;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class PageTest {

    @Test
    void computes_total_pages_and_navigation_flags() {
        Page<String> page = Page.of(List.of("a", "b"), 0, 2, 5);

        assertThat(page.totalPages()).isEqualTo(3);
        assertThat(page.hasNext()).isTrue();
        assertThat(page.hasPrevious()).isFalse();
    }

    @Test
    void map_transforms_content_but_keeps_pagination_metadata() {
        Page<Integer> page = Page.of(List.of(1, 2, 3), 1, 3, 10);

        Page<String> mapped = page.map(Object::toString);

        assertThat(mapped.content()).containsExactly("1", "2", "3");
        assertThat(mapped.pageNumber()).isEqualTo(1);
        assertThat(mapped.totalElements()).isEqualTo(10);
    }
}
