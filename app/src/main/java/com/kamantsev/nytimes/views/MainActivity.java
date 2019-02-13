package com.kamantsev.nytimes.views;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;


public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.initialize(getApplicationContext());//початкова ініціацізація і завантаження даних з бази

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.vp_main);
        setupViewPager();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(){
        CategoriesTabsAdapter adapter = new CategoriesTabsAdapter(getSupportFragmentManager());

        adapter.addTab(ContentListFragment.newInstance(Category.EMAILED), Category.EMAILED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.SHARED), Category.SHARED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.VIEWED), Category.VIEWED.toString());
        adapter.addTab(ContentListFragment.newInstance(Category.FAVORITE), Category.FAVORITE.toString());

        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
}

/*TODO try to migrate to androidx
check all values placed in value's folder and styles are used
Create preferences & light/dark theme choose
*/
