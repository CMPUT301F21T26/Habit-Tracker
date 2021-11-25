package com.cmput301f21t26.habittracker.ui.search;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.cmput301f21t26.habittracker.MobileNavigationDirections;
import com.cmput301f21t26.habittracker.R;
import com.cmput301f21t26.habittracker.ui.UserListAdapter;
import com.cmput301f21t26.habittracker.interfaces.UserListCallback;
import com.cmput301f21t26.habittracker.objects.OtherUserController;
import com.cmput301f21t26.habittracker.objects.User;
import com.cmput301f21t26.habittracker.ui.MainActivity;

import java.util.ArrayList;

public class SearchFragment extends Fragment {
    private SearchView searchView;
    private ArrayList<User> usersList;
    private UserListAdapter userListAdapter;
    private ListView userListView;
    private OtherUserController otherUserController;
    private NavController navController;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        usersList = new ArrayList<>();
        otherUserController = OtherUserController.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment_activity_main);

        userListView = view.findViewById(R.id.searchListView);
        if (getActivity() != null) {
            userListAdapter = new UserListAdapter(getActivity(), usersList);
            userListView.setAdapter(userListAdapter);
        }

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User otherUser = (User) adapterView.getItemAtPosition(i);
                otherUserController.getHabitList(otherUser, updatedOtheruser -> {
                    NavDirections action = MobileNavigationDirections.actionGlobalNavigationProfile(updatedOtheruser);
                    navController.navigate(action);
                });
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MainActivity.hideMenuItems(menu);

        MenuItem searchClickedItem = menu.findItem(R.id.action_clicked_search);
        searchClickedItem.setVisible(true);
        searchClickedItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchClickedItem.setIcon(null);
        // Get and set our SearchView

        searchView = (SearchView) searchClickedItem.getActionView();
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(false);        // Make it so user cannot collapse the search view
        searchView.onActionViewExpanded();      // Expand search view when fragment is opened
        searchView.setQueryHint("Search a username");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                otherUserController.getUsersList(newText, new UserListCallback() {
                    @Override
                    public void onCallback(ArrayList<User> listOfUsers) {
                        usersList = listOfUsers;
                        userListAdapter = new UserListAdapter(getActivity(), usersList);
                        userListView.setAdapter(userListAdapter);
                        Log.d("SearchFragment", usersList.toString());
                    }
                });
                return false;
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.hideBottomNav(getActivity().findViewById(R.id.addHabitButton),
                getActivity().findViewById(R.id.extendBottomNav));
        // Add back button
        if (((MainActivity) getActivity()) != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        MainActivity.showBottomNav(getActivity().findViewById(R.id.addHabitButton),
                getActivity().findViewById(R.id.extendBottomNav));
        // remove back button
        if (((MainActivity) getActivity()) != null) {
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((MainActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
            // close keyboard when going back
            final InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
            if (inputMethodManager.isActive()) {
                if (getActivity().getCurrentFocus() != null) {
                    inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                }
            }
        }

    }
}