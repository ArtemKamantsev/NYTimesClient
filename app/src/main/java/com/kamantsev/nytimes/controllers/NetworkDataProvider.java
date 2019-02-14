package com.kamantsev.nytimes.controllers;

import android.util.Log;
import android.widget.ImageView;

import com.google.gson.GsonBuilder;
import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.Category;
import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.NYTResponse;
import com.kamantsev.nytimes.models.request_model.ResultEmailed;
import com.kamantsev.nytimes.models.request_model.ResultShared;
import com.kamantsev.nytimes.models.request_model.ResultViewed;
import com.squareup.picasso.Picasso;

import java.util.LinkedList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.kamantsev.nytimes.controllers.DataManager.DataModifiedListener;

//Network interaction logic
class NetworkDataProvider {

    private static final String api_key = "Zw2O9kvGDEB5ytPocGdybvc8miwf02aV";//api-key for authorization in data request
    private static final String baseUrl = "https://api.nytimes.com/svc/mostpopular/v2/";//static part of request

    private static NYTAPI api;


    static {
        //Create API object for requests
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()))
                .build();
        api = retrofit.create(NYTAPI.class);
    }

    static void requestData(final Category category, DataModifiedListener listener) {
        switch (category) {
            case EMAILED:
                api.getMostEmailed(api_key).enqueue(new RequestCallback<ResultEmailed>(category, listener));
                break;
            case SHARED:
                api.getMostShared(api_key).enqueue(new RequestCallback<ResultShared>(category, listener));
                break;
            case VIEWED:
                api.getMostViewed(api_key).enqueue(new RequestCallback<ResultViewed>(category, listener));
                break;
            default:
                throw new IllegalArgumentException("Wrong category!");
        }
    }

    static void loadImage(ImageView imageView, String url) {
        //Load specified picture
        Picasso.get()
                .load(url)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.error)
                .into(imageView);
    }

    private static class RequestCallback<T extends AbstractResult> implements Callback<NYTResponse<T>> {

        private Category category;
        private DataModifiedListener responseListener;

        RequestCallback(Category category, DataModifiedListener responseListener) {
            this.category = category;
            this.responseListener = responseListener;
        }

        @Override
        public void onResponse(Call<NYTResponse<T>> call, Response<NYTResponse<T>> response) {
            if (response.body() != null) {
                //Convert response object's to Articles
                List<Article> articles = new LinkedList<>();
                for (AbstractResult result : response.body().getResults()) {
                    Article article = new Article(result, category);
                    articles.add(article);
                }
                DataManager.setCategory(category, articles);//set new Articles
                //notify category data changed successfully
                responseListener.onDataModified(DataModifiedListener.Status.CATEGORY_LOADED);
            } else {
                //notify category data request failure
                responseListener.onDataModified(DataModifiedListener.Status.CATEGORY_LOADING_FAILED);
            }
        }

        @Override
        public void onFailure(Call<NYTResponse<T>> call, Throwable t) {
            Log.e(category.toString(), t.toString());//log failure
            //notify category data request failure
            responseListener.onDataModified(DataModifiedListener.Status.CATEGORY_LOADING_FAILED);
        }
    }
}
