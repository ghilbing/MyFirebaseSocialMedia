package com.hilbing.myfirebasesocialmedia.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hilbing.myfirebasesocialmedia.DashboardActivity;
import com.hilbing.myfirebasesocialmedia.MainActivity;
import com.hilbing.myfirebasesocialmedia.R;
import com.hilbing.myfirebasesocialmedia.activities.AddPostActivity;
import com.hilbing.myfirebasesocialmedia.adapters.AdapterUsers;
import com.hilbing.myfirebasesocialmedia.models.ModelUser;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UsersFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    @BindView(R.id.users_rv)
    RecyclerView usersRv;

    AdapterUsers adapterUsers;
    List<ModelUser> userList;

    FirebaseAuth mAuth;



    public UsersFragment() {
        // Required empty public constructor
    }

    public static UsersFragment newInstance(String param1, String param2) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);

        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_users, container, false);
        //init RecyclerView
        ButterKnife.bind(this, view);
        //init mAuth
        mAuth = FirebaseAuth.getInstance();
        //Set RV properties
        usersRv.setHasFixedSize(true);
        usersRv.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        userList = new ArrayList<>();
        //get all users
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        //Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named Users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    //get all users except currently signed in user
                    if(!modelUser.getUid().equals(user.getUid())){
                        userList.add(modelUser);
                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //set adapter to recyclerview
                    usersRv.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(String query) {
        //Get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database named Users
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        //get all data from path
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for(DataSnapshot ds: snapshot.getChildren()){
                    ModelUser modelUser = ds.getValue(ModelUser.class);
                    //Conditions to fulfil search:
                    //user not current user and the user name or email contains text entered in SearchView

                    //get all searched users except currently signed in user
                    if(!modelUser.getUid().equals(user.getUid())){
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase()) ||
                        modelUser.getEmail().toLowerCase().contains(query.toLowerCase())){
                            userList.add(modelUser);
                        }


                    }
                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(), userList);
                    //set adapter to recyclerview
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adapter to recyclerview
                    usersRv.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);

        //hide add post icon from this fragment
        menu.findItem(R.id.action_add_post).setVisible(false);

        //SearchView
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        //Search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user presses search button from keyboard
                //if search query is not empty, then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text
                    searchUsers(s);
                } else {
                    //search text empty, get all users
                    getAllUsers();

                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user presses any single letter
                //if search query is not empty, then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text
                    searchUsers(s);
                } else {
                    //search text empty, get all users
                    getAllUsers();

                }
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }



    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //get item id
        int id = item.getItemId();
        if(id == R.id.action_logout){
            mAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here

        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}