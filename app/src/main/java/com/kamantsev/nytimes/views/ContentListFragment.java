package com.kamantsev.nytimes.views;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;

//Represent specific category
public class ContentListFragment extends Fragment
        implements DataManager.DataModifiedListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String ARG_CATEGORY = "category";

    private Category category;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageView ivPlaceholder;
    private ContentAdapter adapter;

    public static ContentListFragment newInstance(Category category) {
        ContentListFragment fragment = new ContentListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY, category.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getArguments()!=null){
            String sCategory = getArguments().getString(ARG_CATEGORY);
            this.category = Category.valueOf(sCategory);

            adapter = new ContentAdapter(category);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_content_list, container, false);
    }
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View rootView = getView();
        recyclerView = rootView.findViewById(R.id.rv_content);
        ivPlaceholder = rootView.findViewById(R.id.iv_placeholder);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        if(category == Category.FAVORITE){
            new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                    return false;
                }

                @Override
                public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                    Long id = DataManager.getArticle(category, viewHolder.getAdapterPosition())
                            .getArticleExtra().getId();
                    DataManager.tryToRemoveFromFavorite(getActivity(), id);
                }
            }).attachToRecyclerView(recyclerView);
        }

        swipeRefreshLayout = rootView.findViewById(R.id.srl_content);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(true);
                initiateDataLoading();
            }
        });

        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        //Attach fragment to control data changes of specific category
        DataManager.registerOnDataModifiedListener(category, this);
        initView();
    }

    @Override
    public void onStop() {
        super.onStop();
        //Remove article from listeners of specific data category
        //Due to lifecycles it has to be placed here
        DataManager.unregisterOnDataModifiedListener(category, this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        //Saving attached category
        outState.putString(ARG_CATEGORY, category.toString());
    }

    @Override
    public void onRefresh() {
        initiateDataLoading();
    }

    @Override
    public void onDataModified(Status status) {
        initView();
    }

    private void initView(){
        swipeRefreshLayout.setRefreshing(false);//Remove loading circle
        if(DataManager.getArticleCount(category)>0) {
            recyclerView.setVisibility(View.VISIBLE);
            ivPlaceholder.setVisibility(View.GONE);
            adapter.notifyDataSetChanged();//force the recycleView to refresh category
        }else{
            recyclerView.setVisibility(View.GONE);
            ivPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    private void initiateDataLoading(){
        //Force to reload specific category
        DataManager.loadCategory(category);
    }
}
