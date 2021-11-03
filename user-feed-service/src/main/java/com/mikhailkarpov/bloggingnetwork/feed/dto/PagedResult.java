package com.mikhailkarpov.bloggingnetwork.feed.dto;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class PagedResult<T> {

    private List<T> result;
    private int page;
    private int totalPages;
    private long totalResults;

    public PagedResult(Page<T> page) {
        this.result = page.getContent();
        this.page = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalResults = page.getTotalElements();
    }

    @Override
    public String toString() {

        return "PagedResult{" +
                "page=" + this.page +
                ", size=" + this.result.size() +
                ", totalPages=" + this.totalPages +
                ", totalResults=" + totalResults +
                '}';
    }
}
