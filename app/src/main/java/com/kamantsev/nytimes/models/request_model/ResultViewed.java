package com.kamantsev.nytimes.models.request_model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultViewed extends AbstractResult {

    @SerializedName("views")
    @Expose
    private Integer views;

    public Integer getViews() {
        return views;
    }
}