package com.kamantsev.nytimes.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;

public class ArticleActivity extends AppCompatActivity {

    private static final String ID_KEY = "id";
    private static final int INDEX_DEFAULT = -1;

    //Тексти для вікна підтвердження видалення статті з "Favorite"
    private static final String alertTitle="Removal confirmation",
                                alertMessage="This article will be removed from \"Favorite\" tab" +
                                        " and from device storage. Are you sure you want to remove it?",
                                alertBtn1="Yes",
                                alertBtn2="No";

    private WebView webView;//контейнер для статті
    private Article article;//поточна стаття

    public static Intent getIntent(Context context, long articleId){
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ID_KEY, articleId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setWebView();

        long articleId = INDEX_DEFAULT;
        if(savedInstanceState != null){
            articleId = savedInstanceState.getLong(ID_KEY,INDEX_DEFAULT);
        }else{
           articleId = getIntent().getLongExtra(ID_KEY,INDEX_DEFAULT);
        }
        article = DataManager.getArticle(articleId);
        if(article != null) {
            loadContent();
        }else{
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ID_KEY, article.getArticleExtra().getId());
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
        if(article!=null) {
            getMenuInflater().inflate(R.menu.article_menu, menu);
            if (article.isBelong(Category.FAVORITE))
                menu.findItem(R.id.action_favorite).setIcon(R.drawable.favorite);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
        }else
            if (item.getItemId()==R.id.action_favorite) {
                setFavorite(item);
            }
        return super.onOptionsItemSelected(item);
    }

    private void setFavorite(final MenuItem item){
        if (article.isBelong(Category.FAVORITE)) {//Прибираємо з "Favorite"
            //Відображаємо вікно підтвердження видалення.
            AlertDialog.Builder alertDialog=new AlertDialog.Builder(this);
            alertDialog.setTitle(alertTitle);  // заголовок
            alertDialog.setMessage(alertMessage); // повідомлення
            alertDialog.setPositiveButton(alertBtn1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    DataManager.removeFromFavorite(article, new DataManager.FileOperationCallback() {
                        @Override
                        public void onSucceed() {
                            item.setIcon(R.drawable.favorite_border);
                            showToast("Article was removed successfully.");
                        }

                        @Override
                        public void onFailure(String message) {
                            showToast(message);
                        }
                    });
                }
            });
            alertDialog.setNegativeButton(alertBtn2, null);//нічого не робимо
            alertDialog.setCancelable(true);
            alertDialog.show();

        } else {
            //Починаємо скачування сторінки
            item.setIcon(R.drawable.loading);
            DataManager.addToFavorite(article, new DataManager.FileOperationCallback() {
                @Override
                public void onSucceed() {
                    item.setIcon(R.drawable.favorite);
                    showToast("Page was saved.");
                    loadContent();
                }

                @Override
                public void onFailure(String mesage) {
                    item.setIcon(R.drawable.favorite_border);
                    showToast("Unfortunately page isn't saved.");
                }
            });
        }
    }

    private void setWebView(){
        webView=findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
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
    }

    private void showToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }

    private void loadContent(){
        Log.e("loadContent", article.getArticleExtra().getPath());
        webView.loadUrl(article.getArticleExtra().getPath());
    }
}