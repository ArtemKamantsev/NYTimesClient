package com.kamantsev.nytimes.models;

import com.kamantsev.nytimes.models.request_model.AbstractResult;

import java.util.HashSet;
import java.util.Set;


public class Article {

    private AbstractResult articleExtra;//article's descriptive information
    private Set<Category> categories;//categories to which article belongs

    public Article(AbstractResult articleExtra, Category initialCategory) {
        this.articleExtra = articleExtra;
        this.categories = new HashSet<>();
        this.categories.add(initialCategory);
    }

    @Override
    public boolean equals(Object obj) {
        boolean res = false;
        if (obj instanceof Article) {
            if (articleExtra.getId() == ((Article) obj).articleExtra.getId()) {
                res = true;
            }
        }
        return res;
    }

    @Override
    public int hashCode() {
        return (int) (articleExtra.getId() % Integer.MAX_VALUE);
    }

    public AbstractResult getArticleExtra() {
        return articleExtra;
    }

    public void addCategory(Category category) {
        categories.add(category);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
    }

    public boolean isBelong(Category category) {
        return categories.contains(category);
    }

    public int getCategoriesCount() {
        return categories.size();
    }

    //In case of articles' collision
    public void updateData(Article article) {
        this.articleExtra.setPath(article.getArticleExtra().getPath());
    }
}
