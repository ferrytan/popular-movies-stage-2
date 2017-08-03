package com.meetferrytan.popularmovies.rest;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by ferrytan on 11/3/16.
 * An object that contains API response for collection request with paging
 *
 *  json structure:
 *
 {
     "page": 3,
     "total_results": 19504,
     "total_pages": 976,
     "results": [] // array of object Movies
 }
 */

public class ResponseCollection<T> {
    @SerializedName("page")
    private int mPage;
    @SerializedName("total_results")
    private int mTotalResult;
    @SerializedName("total_pages")
    private int mTotalPages;
    @SerializedName("results")
    private List<T> mResults;

    public ResponseCollection() {
    }

    public int getPage() {
        return mPage;
    }

    public void setPage(int page) {
        mPage = page;
    }

    public int getTotalResult() {
        return mTotalResult;
    }

    public void setTotalResult(int totalResult) {
        mTotalResult = totalResult;
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    public void setTotalPages(int totalPages) {
        mTotalPages = totalPages;
    }

    public List<T> getResults() {
        return mResults;
    }

    public void setResults(List<T> results) {
        mResults = results;
    }
}
