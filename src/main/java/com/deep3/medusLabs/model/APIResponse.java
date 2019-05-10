package com.deep3.medusLabs.model;

import org.apache.commons.collections4.IteratorUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;


public class APIResponse<T> {

    private List<T> content;
    private HttpStatus httpStatus;

    public APIResponse(HttpStatus httpStatus)
    {
        setHttpStatus(httpStatus);
        setContent(new ArrayList());
    }

    public APIResponse(HttpStatus httpStatus, Iterable<T> results)
    {
        setHttpStatus(httpStatus);
        setContent(IteratorUtils.toList(results.iterator()));
        setContent(new ArrayList());
        getContent().addAll(IteratorUtils.toList(results.iterator()));
    }

    public APIResponse(HttpStatus httpStatus, List<T> results)
    {
        setHttpStatus(httpStatus);
        setContent(new ArrayList());
        getContent().addAll(results);
    }

    public APIResponse(HttpStatus httpStatus, T result)
    {
        setHttpStatus(httpStatus);
        setContent(new ArrayList());
        getContent().add(result);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public List<T> getContent() {
        return content;
    }

    public void setContent(List<T> content) {
        this.content = content;
    }
}
