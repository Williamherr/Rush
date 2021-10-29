package com.example.rush;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ClassesFragment extends Fragment {

    FloatingActionButton fabButton, fabButton2, fabButton3;
    Animation fabOpen, fabClose;
    boolean isOpen = false;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView noClasses;
    private RecyclerView classView;
    private RecyclerView.Adapter adapter;
    private ArrayList<ClassInfo> listOfClasses = new ArrayList<>();
    String userID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseFirestore.getInstance();
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
            This section initializes the RecyclerView to show the user's classes
         */
        RecyclerView recycle = (RecyclerView) view.findViewById(R.id.classes);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recycle.setLayoutManager(manager);
        recycle.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        /*
            This section is used for the FloatingActionButton menu for creating/joining a class
            and deleting classes
         */
        fabButton = view.findViewById(R.id.classOptionsButton);
        fabButton2 = view.findViewById(R.id.classDeleteButton);
        fabButton3 = view.findViewById(R.id.classEditButton);
        noClasses = view.findViewById(R.id.noClassesText);
        fabOpen = AnimationUtils.loadAnimation
                (getActivity(), R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation
                (getActivity(), R.anim.fab_close);
        /*
            The below sets up the onClickListener for each button
         */

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
            //Runs a query for any classes created by the current user
            database.collection("classes")
                    .whereEqualTo("createdBy", userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Success", document.getId() + " => " + document.getData());
                                    //Cast the QueryDocumentSnapshot into a ClassInfo obj
                                    try {
                                        ClassInfo obj = document.toObject(ClassInfo.class);
                                        //Keep track of all classes created by this user
                                        listOfClasses.add(obj);
                                        adapter = new ClassAdapter(listOfClasses);
                                        recycle.setAdapter(adapter);
                                    } catch (NullPointerException e) {
                                        Toast.makeText(getActivity(), "Sorry, something went wrong!"
                                                , Toast.LENGTH_SHORT).show();
                                    }

                                }
                            } else {
                                Log.d("Error", "Error getting documents: ", task.getException());
                            }
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
            fabButton2.startAnimation(fabClose);
            fabButton2.setClickable(false);
            fabButton3.startAnimation(fabClose);
            fabButton3.setClickable(false);
            isOpen = false;
        } else {
            fabButton2.startAnimation(fabOpen);
            fabButton2.setClickable(true);
            fabButton3.startAnimation(fabOpen);
            fabButton3.setClickable(true);
            isOpen = true;
        }
    }

}