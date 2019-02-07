package com.kamantsev.nytimes.views;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.kamantsev.nytimes.R;
import com.kamantsev.nytimes.controllers.DataManager;
import com.kamantsev.nytimes.models.Category;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ViewPager mViewPager;

    private int statusIconId;//ID картинки, що мусить наразі бути іконкою статусу
    private MenuItem statusIcon;//іконка статусу
    private Map<String,String> inconsistencies;//список неспівпадінь у імені категорії у меню та у запиті

    {
        statusIconId=R.drawable.loading;
        inconsistencies=new HashMap<>();
        inconsistencies.put("Membercenter","membercenter");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DataManager.initialize(getApplicationContext());//початкова ініціацізація і завантаження даних з бази

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        mViewPager = findViewById(R.id.vp_main);
        setupViewPager();

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(mViewPager);
    }

    private void setupViewPager(){
        CategoriesTabsAdapter adapter = new CategoriesTabsAdapter(getSupportFragmentManager());

        for(Category category : Category.values()){
            adapter.addTab(ContentListFragment.newInstance(category), category.toString());
        }

        mViewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        statusIcon = menu.findItem(R.id.status_indicator);
        statusIcon.setIcon(statusIconId);
        initNavView();
        return true;
    }

    private void initNavView(){
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {

                        menuItem.setChecked(true);
                        drawerLayout.closeDrawers();

                        String section = menuItem.getTitle().toString();
                        if(inconsistencies.containsKey(section))
                            section=inconsistencies.get(section);

                        //DataManager.loadData(section);

                        return true;
                    }
                });
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item){
        if (item.getItemId()==R.id.status_indicator) {
            if(statusIconId==R.drawable.reload) {//якщо картинка позначає перезавантаження даних
                DataManager.loadCategories();
            }
        }
        return true;
    }

    //TODO reimplement
    private void setStatusIcon(){
        //Змінюємо іконку статусу відповідно до станів завантаження даних з мережі та бази даних

        //Якщо хоч щось завантажується, то значок завантаження
        /*if(favoriteLoading==LoadingStatus.LOADING || networkLoading==LoadingStatus.LOADING)
            statusIconId = R.drawable.loading;
        else
            //Якщо помилка при завантаженні з мережі(при завантаженні з бд помилки не може бути) даємо користувачу можливість оновити дані
            if(networkLoading==LoadingStatus.FAILED)
                statusIconId = R.drawable.reload;
            else
                statusIconId = R.drawable.ok;//Якщо завантаження пройшли успішно
        //Відразу встановлюємо потрібну іконку, якщо меню вже створено.
        // Метод може бути викликаний до створення меню і ініціалізації змінної statusIcon
        if(statusIcon!=null)
            statusIcon.setIcon(statusIconId);*/
    }
}

//TODO try to migrate to androidx