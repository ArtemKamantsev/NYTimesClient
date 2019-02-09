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

public class ArticleActivity extends AppCompatActivity implements DataManager.DataLoadingListener {

    private static final String ID_KEY = "id";
    private static final Long INDEX_DEFAULT = -1L;

    //Тексти для вікна підтвердження видалення статті з "Favorite"
    private static final String alertTitle = "Removal confirmation",
            alertMessage = "This article will be removed from \"Favorite\" tab" +
                    " and from device storage. Are you sure you want to remove it?",
            alertBtn1 = "Yes",
            alertBtn2 = "No";

    private WebView webView;//контейнер для статті
    private Long articleID;//поточна стаття
    private MenuItem itemFavorite;

    public static Intent getIntent(Context context, long articleId) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ID_KEY, articleId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setWebView();

        articleID = INDEX_DEFAULT;
        if (savedInstanceState != null) {
            articleID = savedInstanceState.getLong(ID_KEY, INDEX_DEFAULT);
        } else {
            articleID = getIntent().getLongExtra(ID_KEY, INDEX_DEFAULT);
        }

        if (DataManager.getArticle(articleID) != null) {
            loadContent();
        } else {
            finish();
        }

        DataManager.registerOnDataChangeListener(Category.FAVORITE, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.unregisterOnDataChangeListener(Category.FAVORITE, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(ID_KEY, articleID);
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
        itemFavorite = menu.findItem(R.id.action_favorite);
        setFavoriteIcon();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_favorite) {
            setFavorite();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadingSucceed() {
        setFavoriteIcon();
        Article article = DataManager.getArticle(articleID);
        if(article.isBelong(Category.FAVORITE)){
            showToast("Article saved successfully!");
        }else{
            showToast("Article was removed successfully!");
        }
    }

    @Override
    public void onLoadingFailed() {
        setFavoriteIcon();
        Article article = DataManager.getArticle(articleID);
        if(article.isBelong(Category.FAVORITE)){
            showToast("Article wasn't removed successfully!");
        }else{
            showToast("Article wasn't saved!");
        }
    }



    private void setFavorite() {
        Article article = DataManager.getArticle(articleID);
        if (article.isBelong(Category.FAVORITE)) {//Прибираємо з "Favorite"
            //Відображаємо вікно підтвердження видалення.
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setTitle(alertTitle);  // заголовок
            alertDialog.setMessage(alertMessage); // повідомлення
            alertDialog.setPositiveButton(alertBtn1, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg1) {
                    DataManager.removeFromFavorite(articleID);
                }
            });
            alertDialog.setNegativeButton(alertBtn2, null);//нічого не робимо
            alertDialog.setCancelable(true);
            alertDialog.show();

        } else if (!article.isBelong(Category.LOADING)) {
            //Починаємо скачування сторінки
            itemFavorite.setIcon(R.drawable.loading);
            DataManager.addToFavorite(articleID);
        }
    }

    private void setWebView() {
        webView = findViewById(R.id.web_view);
        WebSettings webSettings = webView.getSettings();

        //could be uncommented, but will enlarge loading time
//        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setJavaScriptEnabled(true);

        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);
            }
        });
    }

    private void loadContent() {
        Article article = DataManager.getArticle(articleID);
        webView.loadUrl(article.getArticleExtra().getPath());
    }

    private void setFavoriteIcon() {
        Article article = DataManager.getArticle(articleID);
        if (itemFavorite != null && article != null) {
            if (article.isBelong(Category.FAVORITE)) {
                itemFavorite.setIcon(R.drawable.favorite);
            } else if (article.isBelong(Category.LOADING)) {
                itemFavorite.setIcon(R.drawable.loading);
            } else {
                itemFavorite.setIcon(R.drawable.favorite_border);
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}