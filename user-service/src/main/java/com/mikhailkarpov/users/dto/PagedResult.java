package com.mikhailkarpov.users.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED) // for JSON
public class PagedResult<T> {

    @JsonProperty("result")
    private List<T> result;

    @JsonProperty("page")
    private int page;

    @JsonProperty("totalPages")
    private int totalPages;

    @JsonProperty("totalResults")
    private long totalResults;

    public PagedResult(List<T> result, int page, int totalPages, long totalResults) {
        this.result = result;
        this.page = page;
        this.totalPages = totalPages;
        this.totalResults = totalResults;
    }

    public PagedResult(Page<T> page) {
        this.result = page.getContent();
        this.page = page.getNumber();
        this.totalPages = page.getTotalPages();
        this.totalResults = page.getTotalElements();
    }

    @Override
    public String toString() {

        StringBuilder result = new StringBuilder();
        for (T t : this.result) {
            result.append(t);
        }

        return "PagedResult{" +
                "result=" + result +
                ", page=" + page +
                ", totalPages=" + totalPages +
                ", totalResults=" + totalResults +
                '}';
    }
}
