package com.example.rush;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import com.example.rush.messages.CreatePrivateMessages;
import com.example.rush.messages.MessageFragment;
import com.example.rush.messages.PrivateChatFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.net.Authenticator;


public class MainActivity extends AppCompatActivity implements
        MessageFragment.MessageFragmentListener,LoginFragment.CreateFragmentListener,
        NotificationFragment.NotificationFragmentListener, AddPhotoFragment.UploadFragmentListener, ClassesFragment.ClassDetailFragmentListener
{
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String uid;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setVisibility(View.INVISIBLE);
        //Navigation bar
        bottomNavigation();

        if (user != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }

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
                    case "Notifications": // Notifications
                        Log.d("navBar", "Notifications");
                        notificationFragment();
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

    public void notificationFragment() {
        setTitle("Notification");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new NotificationFragment()).addToBackStack(null)
                .commit();
    }

    /*
    *
    * This function add a new photo feature using the Notification to call this feature for testing.
    * I will integrate this feature into another page,
    * such as create account that let the user could upload the image
    * */
    @Override
    public void addNewPhotoFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AddPhotoFragment(), "UploadFragment")
                .addToBackStack(null)
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
    public void createNewMessages() {
        setTitle("New Messages");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new CreatePrivateMessages())
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

    @Override
    public void backFragment() {
        getSupportFragmentManager().popBackStack();
    }
    
    public void goToClassDetails(String name, String instructor, String description) {
        setTitle(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView,
                new ClassDetailsFragment(name, instructor, description)).addToBackStack(null).commit();
    }
}