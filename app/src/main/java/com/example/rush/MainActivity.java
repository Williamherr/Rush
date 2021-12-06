package com.example.rush;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;


import com.example.rush.View.fragments.account.AccountCreationFragment;
import com.example.rush.View.fragments.AddPhotoFragment;
import com.example.rush.View.fragments.account.AccountFragment;
import com.example.rush.View.fragments.account.LoginFragment;
import com.example.rush.View.fragments.classes.ClassChatFragment;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity implements MessageFragment.MessageFragmentListener, LoginFragment.CreateFragmentListener, ClassesFragment.ClassDetailFragmentListener,
        PrivateChatFragment.PrivateChatFragmentListener, AddPhotoFragment.UploadFragmentListener, AccountCreationFragment.AccountCreationFragmentListener, AccountFragment.IAccountSettingInterface

//NotificationFragment.NotificationFragmentListener,

{
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private BottomNavigationView bottomNav;
    private final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_main);
        bottomNav = findViewById(R.id.bottomNavigationBar);
        bottomNav.setVisibility(View.GONE);
        user = FirebaseAuth.getInstance().getCurrentUser();


        //Navigation bar
        bottomNavigation();

        if (user != null) {
            gotoHomeFragment(user);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
        if (user != null) {
            setStatus(true);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (user != null) {
            setStatus(false);
        }
    }

    public void setStatus(boolean start) {
        if (start) {
            db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        String status = (String) task.getResult().get("lastStatus");
                        if (status == null) {
                            status = "available";
                            db.collection("users").document(user.getUid()).update("lastStatus", status);
                        }
                        db.collection("users").document(user.getUid()).update("status", status);

                    }

                }
            });

        } else {
            db.collection("users").document(user.getUid()).update("status", "offline");
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
                    case "Account": // Activity
                        Log.d("navBar", "Account");
                        goToAccount();
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


            default:
                return super.onOptionsItemSelected(item);
        }
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
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            setTitle("Rush");
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("finish", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // To clean up all activities
            startActivity(intent);
            finish();
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
                .replace(R.id.containerView, new ClassesFragment()).addToBackStack(null)
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

    Notification Section (Private Messages)

     */

    /*
     *
     * This function add a new photo feature using the Notification to call this feature for testing.
     * I will integrate this feature into another page,
     * such as create account that let the user could upload the image
     * */
    @Override
    public void addNewPhotoFragment(String messageKey) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AddPhotoFragment(user, messageKey), "UploadFragment")
                .addToBackStack(null)
                .commit();
    }

    /*This is for adding photos to class chat
     */
    public void addNewPhotoFragment(String messageKey, String docID) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AddPhotoFragment(user, messageKey, docID), "UploadFragment")
                .addToBackStack(null)
                .commit();
    }


    @Override
    public void backFragment() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void createNotifications() {
        setTitle("Notification");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new NotificationFragment()).addToBackStack(null)
                .commit();
    }

    public void goToJoinFragment() {
        setTitle("Join Class");
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView, new ClassJoinFragment())
                .addToBackStack(null).commit();
    }

    public void goToClassDetails(String name, String instructor, String description, String id, String createdBy) {
        setTitle(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView,
                new ClassDetailsFragment(name, instructor, description, id, createdBy)).addToBackStack(null).commit();
    }

    public void goToClassChat(String id, String name) {
        setTitle(name);
        getSupportFragmentManager().beginTransaction().replace(R.id.containerView,
                new ClassChatFragment(id, name)).addToBackStack(null).commit();
    }




    /*

 Account Section

  */


    @Override
    public void goToAccountCreationFragment() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AccountCreationFragment())
                .addToBackStack(null)
                .commit();
    }

    public void goToAccount() {
        setTitle("Account");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerView, new AccountFragment(user, db))
                .commit();
    }


}



