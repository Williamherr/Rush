package com.example.rush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation();

    }

    public void bottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setOnItemSelectedListener(new  NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected( MenuItem item) {
                int id = item.getItemId();

                switch (id){
                    case 2131231024: //Home
                        Log.d("navBar","Home");
                        break;
                    case 2131231023: //Groups
                        Log.d("navBar","Groups");
                        break;
                    case 2131231211: //Classes
                        Log.d("navBar","Classes");
                        break;
                    case 2131231025: //Messages
                        Log.d("navBar","Messages");
                        messageFragment();
                        break;
                    case 2131231210: //Activity
                        Log.d("navBar","Activity");
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
}