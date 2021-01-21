package com.hilbing.myfirebasesocialmedia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;
import com.hilbing.myfirebasesocialmedia.fragments.ChatListFragment;
import com.hilbing.myfirebasesocialmedia.fragments.HomeFragment;
import com.hilbing.myfirebasesocialmedia.fragments.ProfileFragment;
import com.hilbing.myfirebasesocialmedia.fragments.UsersFragment;
import com.hilbing.myfirebasesocialmedia.notifications.FirebaseMessaging;
import com.hilbing.myfirebasesocialmedia.notifications.Token;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardActivity extends AppCompatActivity {

    //FirebaseAuth
    private FirebaseAuth mAuth;

    //views
    ActionBar actionBar;
    @BindView(R.id.bottomNavigation)
    BottomNavigationView mBottomNavigation;

    String mUserUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Actionbar and its title
        actionBar = getSupportActionBar();

        //init
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        mBottomNavigation.setOnNavigationItemSelectedListener(selectedListener);

        //home fragment transaction as default
        actionBar.setTitle(getString(R.string.home));
        HomeFragment homeFragment = new HomeFragment();
        FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
        homeFragmentTransaction.replace(R.id.container, homeFragment, "");
        homeFragmentTransaction.commit();

        checkUserStatus();


    }

    public void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token mToken = new Token(token);
        mAuth = FirebaseAuth.getInstance();
        mUserUID = mAuth.getCurrentUser().getUid();
        Log.e("mUserUID", mUserUID);
        reference.child(mUserUID).setValue(mToken.getToken());
        Log.e("DASHBOARDTOKEN", mToken.getToken());
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //handle item clicks
            switch (item.getItemId()){
                case R.id.nav_home:
                    //home fragment transaction
                    actionBar.setTitle(getString(R.string.home));
                    HomeFragment homeFragment = new HomeFragment();
                    FragmentTransaction homeFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    homeFragmentTransaction.replace(R.id.container, homeFragment, "");
                    homeFragmentTransaction.commit();

                    return true;
                case R.id.nav_profile:
                    //profile fragment transaction
                    actionBar.setTitle(getString(R.string.profile));
                    ProfileFragment profileFragment = new ProfileFragment();
                    FragmentTransaction profileFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    profileFragmentTransaction.replace(R.id.container, profileFragment, "");
                    profileFragmentTransaction.commit();

                    return true;
                case R.id.nav_users:
                    //users fragment transaction
                    actionBar.setTitle(getString(R.string.users));
                    UsersFragment usersFragment = new UsersFragment();
                    FragmentTransaction usersFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    usersFragmentTransaction.replace(R.id.container, usersFragment, "");
                    usersFragmentTransaction.commit();

                    return true;
                case R.id.nav_chat:
                    //chats fragment transaction
                    actionBar.setTitle(getString(R.string.chats));
                    ChatListFragment chatListFragment = new ChatListFragment();
                    FragmentTransaction chatsFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    chatsFragmentTransaction.replace(R.id.container, chatListFragment, "");
                    chatsFragmentTransaction.commit();

                    return true;
            }

            return false;
        }
    };

    private void checkUserStatus(){
        //get current user
        FirebaseUser user = mAuth.getCurrentUser();
        if(user != null){
            //user is signed in stay here
            mUserUID = user.getUid();
            //Save currentUID in shared preferences
            SharedPreferences sharedPreferences = getSharedPreferences("SU_USER", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("Current_USERID", mUserUID);
            editor.apply();

            //update token
            String token = FirebaseInstanceId.getInstance().getToken();

            updateToken(token);

        } else {
            //user not signed in, go to MainActivity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onStart() {
        //check on start of app
        checkUserStatus();
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}