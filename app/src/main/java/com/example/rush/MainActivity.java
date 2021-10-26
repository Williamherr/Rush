package com.example.rush;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;


public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Navigation bar
        bottomNavigation();

        //Push notifications
        pushNotifications();
    }

    /*
    * The Push Notification feature now is implementing as a testing.
    * This feature will be integrated into this Rush app.
    * */
    public void pushNotifications(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg =  token;
                        Log.d("Token", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
    }


    public void bottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                switch (id) {
                    case 2131231024: //Home
                        Log.d("navBar", "Home");
                        break;
                    case 2131231023: //Groups
                        Log.d("navBar", "Groups");
                        break;
                    case 2131231211: //Classes
                        Log.d("navBar", "Classes");
                        changeFragment();
                        break;
                    case 2131231025: //Messages
                        Log.d("navBar", "Messages");
                        messageFragment();
                        break;
                    case 2131231210: //Activity
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