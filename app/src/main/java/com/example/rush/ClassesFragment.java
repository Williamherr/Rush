package com.example.rush;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ClassesFragment extends Fragment {

    FloatingActionButton fabButton, fabButton2, fabButton3;
    Animation fabOpen, fabClose, rotateForward, rotateBackward;
    boolean isOpen = false;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView noClasses;
    String userID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //Check if user is not logged in
        if (user != null) {
            userID = user.getUid();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_classes, container, false);
        /*
            This section is used for the FloatingActionButton menu for creating/joining a class
            and deleting classes
         */
        fabButton = view.findViewById(R.id.classOptionsButton);
        fabButton2 = view.findViewById(R.id.classDeleteButton);
        fabButton3 = view.findViewById(R.id.classEditButton);
        noClasses = view.findViewById(R.id.classesText);
        fabOpen = AnimationUtils.loadAnimation
                (getActivity(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation
                (getActivity(), R.anim.fab_close);
        rotateForward = AnimationUtils.loadAnimation
                (getActivity(), R.anim.rotate_forward);
        rotateBackward = AnimationUtils.loadAnimation
                (getActivity(), R.anim.rotate_backward);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();

            }
        });
        fabButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
            }
        });
        fabButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateFab();
                //Switch to classCreationFragment
                ((MainActivity) getActivity()).creationFragment();

            }
        });
        //Check if user is not null
        if (userID != null) {
            dbRef.child(userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    ClassInfo tempClassInfo = snapshot.getValue(ClassInfo.class);
                    //If tempClassInfo is null, there are no classes
                    if (tempClassInfo != null) {
                        Log.d("Name: ", tempClassInfo.getClassName() + "Instructor: " +
                                tempClassInfo.getInstructor());
                    } else {
                        noClasses.setText(R.string.no_classes);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.w("Failed to read value.", error.toException());

                }
            });
        } else {
            /* User shouldn't be able to access this tab when logged out
               Mostly used for testing */
            noClasses.setText(R.string.not_logged_in);
        }


        return view;
    }

    //This method is used to animate the floating action buttons when user clicks on it
    private void animateFab() {
        if (isOpen) {
            fabButton.startAnimation(rotateForward);
            fabButton2.startAnimation(fabClose);
            fabButton2.setClickable(false);
            fabButton3.startAnimation(fabClose);
            fabButton3.setClickable(false);
            isOpen = false;
        } else {
            fabButton.startAnimation(rotateBackward);
            fabButton2.startAnimation(fabOpen);
            fabButton2.setClickable(true);
            fabButton3.startAnimation(fabOpen);
            fabButton3.setClickable(true);
            isOpen = true;
        }
    }
}