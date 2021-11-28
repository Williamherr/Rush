package com.example.rush;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.example.rush.View.fragments.AccountCreationFragment;
import com.example.rush.View.fragments.AddPhotoFragment;
import com.example.rush.View.fragments.classes.ClassCreationFragment;
import com.example.rush.View.fragments.classes.ClassDetailsFragment;
import com.example.rush.View.fragments.classes.ClassJoinFragment;
import com.example.rush.View.fragments.classes.ClassesFragment;
import com.example.rush.View.fragments.HomeFragment;
import com.example.rush.View.fragments.NotificationFragment;
import com.example.rush.View.fragments.messages.CreatePrivateMessages;
import com.example.rush.View.fragments.messages.MessageFragment;
import com.example.rush.View.fragments.messages.PrivateChatFragment;
import com.example.rush.Model.Member;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity implements MessageFragment.MessageFragmentListener, LoginFragment.CreateFragmentListener, ClassesFragment.ClassDetailFragmentListener,
        PrivateChatFragment.PrivateChatFragmentListener,   AddPhotoFragment.UploadFragmentListener, AccountCreationFragment.AccountCreationFragmentListener

//NotificationFragment.NotificationFragmentListener,

{
    private FirebaseUser user;
    private String uid;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setVisibility(View.INVISIBLE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Navigation bar
        bottomNavigation();

        if (user != null) {
            gotoHomeFragment(user);
        }


    }


    public void bottomNavigation() {


        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
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

    // Actionbar Menu select
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isKeyboardOpen())
                    closeKeyboard();
                getSupportFragmentManager().popBackStack();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                return true;

            case R.id.sign_out:
                Log.d("TAG", "Sign out ");
                FirebaseAuth.getInstance().signOut();
                signout();

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        return true;
    }

    public boolean isKeyboardOpen() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isAcceptingText()) {
            return true;
        } else {
            return false;
        }
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    /*

 Home Section

  */
    // Goes Back to the home page when the user has login
    // This will show the navigation bar


    @Override
    public void gotoHomeFragment(FirebaseUser user) {
        this.user = user;
        Log.d("TAG", "gotoHomeFragment: " + user);
        bottomNav.setVisibility(View.VISIBLE);
        HomeFragment();

    }


    // Goes to the Home Fragment
    public void HomeFragment() {
        setTitle("Rush");
        Log.d("TAG", "Sign out " + user);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new HomeFragment())
                .commit();
    }

    public void signout() {
        if (user != null) {
            setTitle("Rush");
            Log.d("TAG", "Sign out ");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(intent);
            finish();
        } else {
            Log.d("TAG", "null ");
        }
    }


    /*

    Message Section (Private Messages)

     */

    // Goes to the message fragment and shows a list of messages.
    public void messageFragment() {
        setTitle("Messages");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new MessageFragment(user))
                .commit();
    }


    @Override
    public void goToPrivateChatFragment(Member otherUser, String messageKey) {
        setTitle("Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new PrivateChatFragment(otherUser, messageKey))
                .addToBackStack(null)
                .commit();
    }


    // Allow users to create a new message
    @Override
    public void createNewMessages(CreatePrivateMessages.iCreatePrivateMessages iListener) {
        setTitle("New Messages");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new CreatePrivateMessages(iListener))
                .setCustomAnimations(R.anim.slide_in_up, R.anim.slide_out_up)
                .addToBackStack(null)
                .commit();
    }


    public void classesFragment() {
        setTitle("Classes");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new ClassesFragment())
                .commit();
    }

    public void creationFragment() {
        setTitle("Create Class");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new ClassCreationFragment()).addToBackStack(null).commit();
    }


    public void notificationFragment() {
        setTitle("Notification");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new NotificationFragment())
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
    public void createNotifications() {
        setTitle("Notification");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new NotificationFragment()).addToBackStack(null)
                .commit();
    }
    public void goToJoinFragment(){
        setTitle("Join Class");
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new ClassJoinFragment())
                .addToBackStack(null).commit();
    }




    @Override
    public void goToAccountCreationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AccountCreationFragment()).commit();
    }

    @Override
    public void backFragment() {
        getSupportFragmentManager().popBackStack();
    }

    public void goToClassDetails(String name, String instructor, String description, String id, String createdBy) {
        setTitle(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView,
                new ClassDetailsFragment(name, instructor, description, id, createdBy)).addToBackStack(null).commit();
    }

}



