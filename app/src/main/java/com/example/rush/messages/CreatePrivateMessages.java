package com.example.rush.messages;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.rush.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;


public class CreatePrivateMessages extends Fragment {

    String TAG = "CreateNewPrivateMessages";

    public CreatePrivateMessages() {
        // Required empty public constructor
    }

    Toolbar topAppBar;
    TextInputLayout textInputLayout;
    TextInputEditText toText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_private_messages, container, false);
        topAppBar = view.findViewById(R.id.topAppBar);

        topAppBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStack();
            }
        });
        toText = view.findViewById(R.id.toText);

        textInputLayout = view.findViewById(R.id.toUser);
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toText.setText("");
            }
        });

        return view;
    }
}