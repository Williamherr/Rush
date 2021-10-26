package com.example.rush;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;


public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation();
    }


    public void bottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                String itemString = item.toString();
                switch (itemString) {
                    case "Home": // Home
                        Log.d("navBar", "Home");
                        break;
                    case "Groups": // Groups
                        Log.d("navBar", "Groups");
                        break;
                    case "Classes": // Classes
                        Log.d("navBar", "Classes");
                        changeFragment();
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
}