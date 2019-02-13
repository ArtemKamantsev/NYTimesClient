package com.kamantsev.nytimes.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;

public class ArticleActivity extends AppCompatActivity implements DataManager.DataModifiedListener {

    private static final String ID_KEY = "id";
    private static final Long INDEX_DEFAULT = -1L;


    private WebView webView;//контейнер для статті
    private Long articleID;//поточна стаття
    private MenuItem itemFavorite;
    private Animation rotation;

    public static Intent getIntent(Context context, long articleId) {
        Intent intent = new Intent(context, ArticleActivity.class);
        intent.putExtra(ID_KEY, articleId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        Toolbar toolbar = findViewById(R.id.article_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setWebView();

        rotation = AnimationUtils.loadAnimation(this, R.anim.rotation);

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

        DataManager.registerOnDataModifiedListener(Category.FAVORITE, this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DataManager.unregisterOnDataModifiedListener(Category.FAVORITE, this);
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
        itemFavorite.setActionView(R.layout.image_view);
        itemFavorite.getActionView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFavorite();
            }
        });
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
    public void onDataModified(Status status) {
        setFavoriteIcon();
        switch (status){
            case ARTICLE_SAVED:
                showToast("Article saved successfully!");
                loadContent();
                break;
            case ARTICLE_SAVING_FAILED:
                showToast("Article wasn't saved.");
                break;
            case ARTICLE_REMOVED:
                showToast("Article removed successfully!");
                break;
            case ARTICLE_REMOVING_FAILED:
                showToast("Article wasn't removed.");
                break;
        }
    }

    private void setFavorite() {
        Article article = DataManager.getArticle(articleID);
        if (article.isBelong(Category.FAVORITE)) {//Прибираємо з "Favorite"
            DataManager.tryToRemoveFromFavorite(this, articleID);
        } else if (!article.isBelong(Category.LOADING)) {
            //Починаємо скачування сторінки
            DataManager.addToFavorite(articleID);
            setFavoriteIcon();
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
            ImageView iv = (ImageView)((FrameLayout)itemFavorite.getActionView()).getChildAt(0);
            iv.clearAnimation();
            if (article.isBelong(Category.FAVORITE)) {
                //itemFavorite.setIcon(R.drawable.ic_baseline_favorite_24px);
                iv.setImageResource(R.drawable.ic_baseline_favorite_24px);
            } else if (article.isBelong(Category.LOADING)) {
                //itemFavorite.setIcon(R.drawable.ic_loading);
                iv.setImageResource(R.drawable.ic_loading);
                iv.startAnimation(rotation);
            } else {
                //itemFavorite.setIcon(R.drawable.favorite_border);
                iv.setImageResource(R.drawable.ic_baseline_favorite_border_24px);
                /*((ImageView)MenuItemCompat.getActionView(itemFavorite)).setImageResource(R.drawable.favorite_border);*/
            }
        }
    }

    private void showToast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
    }
}