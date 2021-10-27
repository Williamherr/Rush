package com.example.rush;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ClassesFragment extends Fragment {

    FloatingActionButton fabButton, fabButton2, fabButton3;
    Animation fabOpen, fabClose, rotateForward, rotateBackward;
    boolean isOpen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


        return view;
    }

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