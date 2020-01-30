package com.example.myapplication.Adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdapterViewPagerUserProfile extends FragmentStatePagerAdapter {

    //ViewPager for user profile
    //(Events created % Events participated by profile owner & Settings(Only if profile belongs to current user))

    //ViewPager properties
    private final List<Fragment> tabFragments = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();

    public AdapterViewPagerUserProfile(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return tabFragments.get(position);
    }

    @Override
    public int getCount() {
        return fragmentTitles.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitles.get(position);
    }

    public void addFragment(Fragment fragment, String title) {
        tabFragments.add(fragment);
        fragmentTitles.add(title);
    }




}
