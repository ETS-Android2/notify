package com.example.myapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterViewPagerFeed extends FragmentStatePagerAdapter {

    //ViewPager for feed page
    //(Event Feed & User Search)

    //ViewPager properties
    private final List<Fragment> tabFragments = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();

    //Constructor
    public AdapterViewPagerFeed(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override   //Get a fragment
    public Fragment getItem(int position) {
        return tabFragments.get(position);
    }

    @Override
    public int getCount() {
        return fragmentTitles.size();
    }

    @Nullable
    @Override   //Get a fragment title
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        tabFragments.add(fragment);
        fragmentTitles.add(title);
    }
}
