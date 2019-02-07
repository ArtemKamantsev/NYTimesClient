package com.kamantsev.nytimes.controllers;

import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//Клас, що працює з мережею
class NetworkDataProvider {

    private static final String api_key = "Zw2O9kvGDEB5ytPocGdybvc8miwf02aV";//ключ ідентицікації на сервері
    private static final String baseUrl="https://api.nytimes.com/svc/mostpopular/v2/";//незмінна частина запиту

    private static NYTAPI api;


    static{
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(
                        new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()) )
                .build();
        api = retrofit.create(NYTAPI.class);
    }

    static void requestData(final Category category){
        switch (category){
            case EMAILED:
                api.getMostEmailed(api_key).enqueue(new RequestCallback<ResultEmailed>(category));
                break;
            case SHARED:
                api.getMostShared(api_key).enqueue(new RequestCallback<ResultShared>(category));
                break;
            case VIEWED:
                api.getMostViewed(api_key).enqueue(new RequestCallback<ResultViewed>(category));
                break;
            default:
                throw new IllegalArgumentException("Wrong category!");
        }
    }

    static void loadImage(ImageView imageView, String url){
        //Завантажуємо картинки за допомогою бібліотеки Picasso
        Picasso.get()//with(DataManager.getContext())
                .load(url)
                .placeholder(R.drawable.loading)
                .error(R.drawable.error)
                .into(imageView);
    }

    private static void failure(String tag, Throwable t){
        Log.e(tag, t.toString());
    }

    private static class RequestCallback<T extends AbstractResult> implements Callback<NYTResponse<T>>{

        private Category category;

        RequestCallback(Category category){
            this.category = category;
        }

        @Override
        public void onResponse(Call<NYTResponse<T>> call, Response<NYTResponse<T>> response) {
            List<Article> articles = new LinkedList<>();
            for(AbstractResult result : response.body().getResults()){
                Article article = new Article(result, category);
                articles.add(article);
            }
            DataManager.setCategory(category, articles);
        }

        @Override
        public void onFailure(Call<NYTResponse<T>> call, Throwable t) {
            failure(category.toString(), t);
        }
    }
}
