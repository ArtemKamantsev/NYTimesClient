package com.kamantsev.nytimes.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
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

            if(category != Category.FAVORITE) {
                DataManager.loadCategory(category);
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_CATEGORY, category.toString());
    }

    public static ContentListFragment newInstance(Category category) {
        ContentListFragment fragment = new ContentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        DataManager.registerOnDataChangeListener(category, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        adapter.notifyDataSetChanged();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        recyclerView = getView().findViewById(R.id.rv_content);
        ivPlaceholder = getView().findViewById(R.id.iv_placeholder);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
        DataManager.unregisterOnDataChangeListener(category, this);
    }

    @Override
    public void onContentChanged() {
        initView();
    }

    private void initView(){
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
