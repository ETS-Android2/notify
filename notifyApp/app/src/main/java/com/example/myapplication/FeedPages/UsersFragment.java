package com.example.myapplication.FeedPages;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.Adapters.AdapterUsers;
import com.example.myapplication.Listeners.OnRecycleItemClickedListener;
import com.example.myapplication.Models.MyUser;
import com.example.myapplication.ViewModels.AuthViewModel;
import com.example.myapplication.ViewModels.UsersViewModel;
import com.example.myapplication.myapplication.notify_beta.R;

import java.util.ArrayList;
import java.util.Arrays;

import static com.example.myapplication.Constants.FILTER_IN_DB;
import static com.example.myapplication.Constants.FILTER_IN_LIST;

public class UsersFragment extends Fragment {

    //RecyclerView and its adapter
    private RecyclerView recyclerView;
    private AdapterUsers adapterUsers;

    //Lists
    private ArrayList<MyUser> userList;
    private ArrayList<String> partList;

    //Toolbar and main activity to reach it
    private AppCompatActivity activityForBar;
    private Toolbar toolbar;

    //ViewModels
    private UsersViewModel usersViewModel;
    private AuthViewModel authViewModel;

    //NavController
    private NavController navController;

    //CustomListener
    private OnRecycleItemClickedListener listener;

    //If fragment created for participants list or wide user search
    private int filterType;

    private static final String TAG = "UserList Fragment: ";


    public UsersFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle("");

        //Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_userlist, container, false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());

        //Set filter type as FILTER_IN_DB as default;
        filterType = FILTER_IN_DB;

        userList = new ArrayList<>();
        //Init list
        if (getArguments() != null) {
            String[] partArray = UsersFragmentArgs.fromBundle(getArguments()).getParticipantsList();
            partList = new ArrayList<>(Arrays.asList(partArray));
            filterType = FILTER_IN_LIST;
        }

        //Init toolBar
        if (toolbar == null) {
            setHasOptionsMenu(true);
            toolbar = view.findViewById(R.id.search_toolBar);
            activityForBar = (AppCompatActivity) getActivity();
            activityForBar.setSupportActionBar(toolbar);
        }

        //Init follow btn listener
        listener = new OnRecycleItemClickedListener() {
            @Override
            public void onItemOfListClicked(Object o) {
                //Navigate depanding on who contains this fragment
                if (filterType == FILTER_IN_DB) {
                    FeedFragmentDirections.ActionFeedFragmentToUserProfileFragment action = FeedFragmentDirections.actionFeedFragmentToUserProfileFragment();
                    action.setUser((MyUser) o);
                    navController.navigate(action);
                    Log.i(TAG, "User with id: " + ((MyUser) o).getId() + " clicked");
                } else if (filterType == FILTER_IN_LIST) {
                    UsersFragmentDirections.ActionUsersFragmentToUserProfileFragment action = UsersFragmentDirections.actionUsersFragmentToUserProfileFragment();
                    action.setUser((MyUser) o);
                    navController.navigate(action);
                }
            }

            @Override
            public void onItemLongClicked(Object o) {

            }
        };

        //Init and set recyclerView, init Adapter
        recyclerView = view.findViewById(R.id.recycler_userlist);
        adapterUsers = new AdapterUsers(getContext(), userList, listener);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(), layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(adapterUsers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Init viewModel
        usersViewModel = ViewModelProviders.of(requireActivity()).get(UsersViewModel.class);
        usersViewModel.UsersViewModel();
        authViewModel = ViewModelProviders.of(requireActivity()).get(AuthViewModel.class);
        authViewModel.AuthViewModel();


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (filterType == FILTER_IN_DB) {
            //If users are already retrieved
            if (usersViewModel.observeUserList().getValue() != null) {
                //Get users
                userList.clear();
                userList = usersViewModel.observeUserList().getValue();
                adapterUsers.setUserList(userList);
                adapterUsers.notifyDataSetChanged();
                Log.i(TAG, "Adapter updated");
            } else {
                //If users are not already retrieved
                usersViewModel.getUserList().observe(getViewLifecycleOwner(), new Observer<ArrayList<MyUser>>() {
                    @Override
                    public void onChanged(ArrayList<MyUser> myUsers) {
                        if (myUsers == null) {
                            //If list returns null
                            Log.e(TAG, "UserList returned null");
                        } else {
                            //Set list to adapter
                            userList.clear();
                            userList = myUsers;
                            adapterUsers.setUserList(userList);
                            adapterUsers.notifyDataSetChanged();
                            Log.i(TAG, "Adapter updated");
                        }
                    }
                });
            }
        } else if (filterType == FILTER_IN_LIST) {
            usersViewModel.getParticipants(partList).observe(getViewLifecycleOwner(), new Observer<ArrayList<MyUser>>() {
                @Override
                public void onChanged(ArrayList<MyUser> myUsers) {
                    //Get participants list to later search in it
                    if (myUsers != null) {
                        userList.clear();
                        userList = myUsers;
                        adapterUsers.setUserList(userList);
                        adapterUsers.notifyDataSetChanged();
                        Log.i(TAG, "Adapter updated");
                    }
                }
            });
        }
        navController = Navigation.findNavController(view);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        //Inflate menu for search action
        activityForBar.getMenuInflater().inflate(R.menu.search_users, menu);
        MenuItem mSearchMenuItem = menu.findItem(R.id.app_bar_search);
        //Inflate SearchView
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        searchView.setMaxWidth(Integer.MAX_VALUE);
        //Handle with query
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (filterType == FILTER_IN_DB) {
                    //Search users in DB
                    usersViewModel.getSearchedUsers(query);
                } else if (filterType == FILTER_IN_LIST) {
                    //Search users locally by filter
                    adapterUsers.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (filterType == FILTER_IN_DB) {
                    //Search users in DB
                    usersViewModel.getSearchedUsers(newText);
                } else if (filterType == FILTER_IN_LIST) {
                    //Search users locally by filter
                    adapterUsers.getFilter().filter(newText);
                }
                return false;
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

}