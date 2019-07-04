package com.samaritan.portchlyt_services;

import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import MainActivityTabs.JobsFragment;
import MainActivityTabs.NewsFragment;
import MainActivityTabs.SearchServicesFragment;
import globals.MyMqtt;
import io.realm.Realm;
import models.mJobs.mJobs;

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    Realm db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.mtoolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setTabIcons();

        //seed
        db = Realm.getDefaultInstance();
        db.beginTransaction();
        for (int i = 0; i < 2; i++) {
            mJobs j = new mJobs();
            j._id = "" + i;
            j.description = "This is job " + i;
            try {
                //db.insert(j);
            } catch (Exception ex) {
                Log.e("hassan", ex.getLocalizedMessage());
            }
        }
        db.commitTransaction();
        db.close();

        if(MyMqtt.mqttClient==null) {//only if client is not already there then re-init
            MyMqtt.init(this);
        }


    }

    private void setTabIcons() {
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_time);//jobs
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_find);//search
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_comment);//blog
        tabLayout.getTabAt(1).select();//select the default tab
    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new JobsFragment(), getString(R.string.jobs));
        adapter.addFragment(new SearchServicesFragment(), getString(R.string.search));
        adapter.addFragment(new NewsFragment(), getString(R.string.blog));
        viewPager.setAdapter(adapter);
    }


}


class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

}