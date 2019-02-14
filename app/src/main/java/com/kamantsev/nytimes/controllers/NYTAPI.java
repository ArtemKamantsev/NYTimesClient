package com.kamantsev.nytimes.controllers;

import com.kamantsev.nytimes.models.request_model.NYTResponse;
import com.kamantsev.nytimes.models.request_model.ResultEmailed;
import com.kamantsev.nytimes.models.request_model.ResultShared;
import com.kamantsev.nytimes.models.request_model.ResultViewed;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface NYTAPI {
    @GET("emailed/30.json")
    Call<NYTResponse<ResultEmailed>> getMostEmailed(@Query("api-key") String apiKey);

    @GET("shared/30.json")
    Call<NYTResponse<ResultShared>> getMostShared(@Query("api-key") String apiKey);

    @GET("viewed/30.json")
    Call<NYTResponse<ResultViewed>> getMostViewed(@Query("api-key") String apiKey);
}
