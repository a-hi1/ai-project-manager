package com.example.backend.entity;

import java.util.List;

public class PageResult<T> {
    private List<T> records;
    private long total;
    private long page;
    private long pageSize;
    private long totalPages;

    public PageResult() {}

    public PageResult(List<T> records, long total, long page, long pageSize) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    public static <T> PageResult<T> of(List<T> records, long total, long page, long pageSize) {
        return new PageResult<>(records, total, page, pageSize);
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public long getPage() { return page; }
    public void setPage(long page) { this.page = page; }
    public long getPageSize() { return pageSize; }
    public void setPageSize(long pageSize) { this.pageSize = pageSize; }
    public long getTotalPages() { return totalPages; }
    public void setTotalPages(long totalPages) { this.totalPages = totalPages; }
}