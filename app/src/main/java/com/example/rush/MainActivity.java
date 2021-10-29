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



public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener, MessageFragment.MessageFragmentListener {

    private String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation();
    }


    public void bottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setVisibility(View.VISIBLE);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                Log.d("navBar", String.valueOf(id));
                switch (id) {
                    case 2131231041: //Home
                        Log.d("navBar", "Home");
                        break;
                    case 2131231040: //Groups
                        Log.d("navBar", "Groups");
                        break;
                    case 2131231039: //Classes
                        Log.d("navBar", "Classes");
                        changeFragment();
                        break;
                    case 2131231042: //Messages
                        Log.d("navBar", "Messages");
                        messageFragment();
                        break;
                    case 2131231038: //Activity
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
                .replace(R.id.containerView, new MessageFragment())
                .commit();
    }
    public void changeFragment() {
        ClassesFragment fragment = new ClassesFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, fragment).commit();
    }

    @Override
    public void changeFragment(int id) {
        if (id == 1) {
            ClassesFragment fragment = new ClassesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout, fragment).commit();
        }
    }




    @Override
    public void goToPrivateChatFragment(String otherUserName, String otherUserId, String messageKey) {
            setTitle("Chat");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.containerView, new PrivateChatFragment(otherUserName,otherUserId,messageKey))
                    .addToBackStack(null)
                    .commit();

    }
}