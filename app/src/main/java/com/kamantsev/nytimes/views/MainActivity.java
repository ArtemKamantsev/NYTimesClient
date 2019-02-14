package com.kamantsev.nytimes.views;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;


public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //should be right here, because of the peculiarities of the app lifecycle
        DataManager.initialize(getApplicationContext());//app initialization and loading data from favorite

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.vp_main);
        setupViewPager();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(){
        CategoriesTabsAdapter adapter = new CategoriesTabsAdapter(getSupportFragmentManager());

        //Creating adapter's tabs
        adapter.addTab(ContentListFragment.newInstance(Category.EMAILED), Category.EMAILED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.SHARED), Category.SHARED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.VIEWED), Category.VIEWED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.FAVORITE), Category.FAVORITE.toString());

        mViewPager.setAdapter(adapter);
    }
}
