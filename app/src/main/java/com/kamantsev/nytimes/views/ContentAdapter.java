package com.kamantsev.nytimes.views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.Category;
import com.kamantsev.nytimes.models.request_model.MediaMetadata;

import java.util.List;


class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ContentHolder> {

    private Category category;

    ContentAdapter(Category category){
        this.category = category;
    }

    @NonNull
    @Override
    public ContentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.content_item, viewGroup, false);
        return new ContentHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContentHolder contentHolder, int i) {
            contentHolder.initialize(i);
    }

    @Override
    public int getItemCount() {
        return DataManager.getArticleCount(category);
    }

    class ContentHolder extends RecyclerView.ViewHolder{

        private TextView tvTitle, tvPreview;
        private ImageView icon;
        private Long articleID;

        ContentHolder(final View itemView){
            super(itemView);

            tvTitle = itemView.findViewById(R.id.item_tv_title);
            tvPreview = itemView.findViewById(R.id.item_tv_preview);
            icon = itemView.findViewById(R.id.item_iv_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = DataManager.getContext();
                    Intent intent = ArticleActivity.getIntent(context, articleID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            tvPreview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    tvPreview.setMaxLines(Integer.MAX_VALUE);
                }
            });
        }

        private void initialize(int index){
            Article article = DataManager.getArticle(category, index);
            articleID = article.getArticleExtra().getId();

            tvTitle.setText(article.getArticleExtra().getTitle());
            tvPreview.setText(article.getArticleExtra().getAbstract());
            List<MediaMetadata> mmd=article.getArticleExtra().getMedia().get(0).getMediaMetadata();
            DataManager.loadImage(icon,mmd.get(1).getUrl());
        }
    }
}
