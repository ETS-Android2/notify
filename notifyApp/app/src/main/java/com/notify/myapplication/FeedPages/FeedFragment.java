package com.notify.myapplication.FeedPages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.notify.myapplication.Adapters.AdapterViewPagerFeed;
import com.notify.myapplication.R;
import com.google.android.material.tabs.TabLayout;

public class FeedFragment extends Fragment {

    private static final String TAG = "UserProfile Fragment: ";

    //ViewPager Container for EventFeed and UserSearch Fragments

    //Variables
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private AdapterViewPagerFeed adapterViewPager;

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Inflate layout
        View view = inflater.inflate(R.layout.feed_holder, container, false);

        //Init variables and add fragments to viewPager

        tabLayout = view.findViewById(R.id.feed_tablayout);
        viewPager = view.findViewById(R.id.feed_viewpager);
        if(adapterViewPager==null){
        adapterViewPager = new AdapterViewPagerFeed(getChildFragmentManager(), AdapterViewPagerFeed.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapterViewPager.addFragment(new EventsFragment(),"Events");
        adapterViewPager.addFragment(new UsersFragment(),"Search");
        }
        viewPager.setAdapter(adapterViewPager);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }
}
