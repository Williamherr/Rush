package com.example.rush;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.example.rush.messages.MessageFragment;
import com.example.rush.messages.PrivateChatFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;




public class MainActivity extends AppCompatActivity implements MessageFragment.MessageFragmentListener,LoginFragment.CreateFragmentListener {

    private String uid;
    BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setVisibility(View.INVISIBLE);
        //Navigation bar
        bottomNavigation();

    }




    public void bottomNavigation() {


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                String itemString = item.toString();
                switch (itemString) {
                    case "Home": // Home
                        Log.d("navBar", "Home");
                        HomeFragment();
                        break;
                    case "Groups": // Groups
                        Log.d("navBar", "Groups");
                        break;
                    case "Classes": // Classes
                        Log.d("navBar", "Classes");
                        classesFragment();
                        break;
                    case "Messages": // Messages
                        Log.d("navBar", "Messages");
                        messageFragment();
                        break;
                    case "Activity": // Activity
                        Log.d("navBar", "Activity");
                        break;
                    default:
                        break;

                }

                return true;
            }
        });

    }


    public void messageFragment() {
        setTitle("Messages");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new MessageFragment()).addToBackStack(null)
                .commit();
    }

    public void classesFragment() {
        setTitle("Classes");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new ClassesFragment()).addToBackStack(null).commit();
    }

    public void creationFragment() {
        setTitle("Create Class");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new ClassCreationFragment()).addToBackStack(null).commit();
    }
    public void HomeFragment() {
        setTitle("Rush");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new HomeFragment()).addToBackStack(null)
                .commit();
    }



    @Override
    public void goToPrivateChatFragment(String otherUserName, String otherUserId, String messageKey) {
            setTitle("Chat");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new PrivateChatFragment(otherUserName,otherUserId,messageKey))
                    .addToBackStack(null)
                    .commit();

    }

    @Override
    public void gotoHomeFragment(String uid) {
        this.uid = uid;
        bottomNav.setVisibility(View.VISIBLE);
        HomeFragment();
    }

    @Override
    public void goToAccountCreationFragment() {

    }
}