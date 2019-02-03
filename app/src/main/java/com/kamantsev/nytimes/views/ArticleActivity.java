package com.kamantsev.nytimes.views;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;

public class ArticleActivity extends AppCompatActivity {

    //Тексти для вікна підтвердження видалення статті з "Favorite"
    private static final String alertTitle="Removal confirmation",
                                alertMessage="This article will be removed from \"Favorite\" tab" +
                                        " and from device storage. Are you sure you want to remove it?",
                                alertBtn1="Yes",
                                alertBtn2="No";
    private WebView webView;//контейнер для статті
    private Article article;//поточна стаття

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        webView=findViewById(R.id.web_view);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view,url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });

        //Отримуємо ідентифікатор статті і завантажуємо її
        Intent intent = getIntent();
        String sCategory= intent.getStringExtra("category");
        Category category = Category.valueOf(sCategory);
        int index = intent.getIntExtra("index",-1);
        loadContent(category, index);
    }

    @Override
    public void onBackPressed() {
        //Підтримка зворотніх переходів у WebView
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_menu, menu);
        if(article.isBelong(Category.FAVORITE))
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.favorite);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId()==R.id.action_favorite) {
            if (article.isBelong(Category.FAVORITE)) {//Прибираємо з "Favorite"
                //Відображаємо вікно підтвердження видалення.
                AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
                alertDialog.setTitle(alertTitle);  // заголовок
                alertDialog.setMessage(alertMessage); // повідомлення
                alertDialog.setPositiveButton(alertBtn1, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int arg1) {
                        //Асинхронно видаляємо статтю
                        new AsyncTask<Void, Void, Boolean>(){
                            @Override
                            protected Boolean doInBackground(Void... params) {
                                if(DataManager.removeFromFavorite(article))
                                    return true;
                                else
                                    return false;
                            }

                            @Override
                            protected void onPostExecute(Boolean result) {
                                if(result) {
                                    item.setIcon(R.drawable.favorite_border);
                                    showToast("Page is removed.");
                                }
                                else {
                                    Log.e("removed","fail");
                                    showToast("Unfortunately page isn't removed.");
                                }
                            }
                        }.execute();
                    }
                });
                alertDialog.setNegativeButton(alertBtn2, null);//нічого не робимо
                alertDialog.setCancelable(true);
                alertDialog.show();

            } else {
                //Починаємо скачування сторінки
                item.setIcon(R.drawable.loading);
                new AsyncTask<Void, Void, Boolean>(){
                    @Override
                    protected Boolean doInBackground(Void... params) {
                        if (DataManager.addToFavorite(article))
                            return true;//якщо завантаження сторінки пройшло успішно
                        else
                            return false;
                    }

                    @Override
                    protected void onPostExecute(Boolean result){
                        if(result) {
                            item.setIcon(R.drawable.favorite);
                            showToast("Page is saved.");
                        }
                        else {
                            item.setIcon(R.drawable.favorite_border);
                            showToast("Unfortunately page isn't saved.");
                        }
                    }
                }.execute();
            }
        }
        return true;
    }
    //для скорочення коду з відображення повідомлень
    private void showToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void loadContent(Category category, int index){//Завантажуємо дані за ідентифікатором статті
        article=DataManager.getArticle(category, index);
        webView.loadUrl(article.getArticleExtra().getUrl());
    }

}