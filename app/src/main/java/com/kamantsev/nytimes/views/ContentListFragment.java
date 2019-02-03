package com.kamantsev.nytimes.views;

import android.content.Context;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Article;
import com.kamantsev.nytimes.models.Category;

public class ContentListFragment extends Fragment implements DataManager.OnDataChangeListener{

    private static final String ARG_CATEGORY = "category";

    private Category category;
    private RecyclerView recyclerView;
    private ImageView ivPlaceholder;
    private ContentAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments()!=null){
            String sCategory = getArguments().getString(ARG_CATEGORY);
            this.category = Category.valueOf(sCategory);

            adapter = new ContentAdapter(category);

            DataManager.registedOnDataChangeListener(category, this);
            DataManager.loadCategory(category);
        }
    }

    public static ContentListFragment newInstance(Category category) {
        ContentListFragment fragment = new ContentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getView().findViewById(R.id.rv_content);
        ivPlaceholder = getView().findViewById(R.id.iv_placeholder);

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onContentChanged() {
        if(DataManager.getArticleCount(category)>0) {
            recyclerView.setVisibility(View.VISIBLE);
            ivPlaceholder.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();//force the recycleView to refresh
        }else{
            recyclerView.setVisibility(View.GONE);
            ivPlaceholder.setVisibility(View.VISIBLE);
        }
    }
}
