package com.kamantsev.nytimes.models;

import com.kamantsev.nytimes.models.request_model.AbstractResult;
import com.kamantsev.nytimes.models.request_model.ResultEmailed;

import java.util.HashSet;
import java.util.Set;


public class Article {

    private AbstractResult articleExtra;//додаткова інформація про статтю, що надійшла із сервера
    private Set<Category> categories;//категорії, до яких належить стаття

    public Article(AbstractResult articleExtra, Category initialCategory){
        this.articleExtra=articleExtra;
        this.categories=new HashSet<>();
        this.categories.add(initialCategory);
    }

    @Override
    public boolean equals(Object obj) {
        boolean res = false;
        if(obj instanceof Article){
            if(articleExtra.getId() == ((Article)obj).articleExtra.getId()){
                res = true;
            }
        }
        return res;
    }

    public AbstractResult getArticleExtra() {
        return articleExtra;
    }

    public boolean addCategory(Category category){
        return categories.add(category);
    }

    public void unfavorite(){
        categories.remove(Category.FAVORITE);
    }

    public boolean isBelong(Category category){ return categories.contains(category);}
}
