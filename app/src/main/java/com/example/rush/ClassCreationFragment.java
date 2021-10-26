package com.example.rush;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ClassCreationFragment extends Fragment {
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private OnFragmentInteractionListener listener;
    String userID;
    Button createButton;
    EditText className, instructorName, classDescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        //Might need to check that userID is not NULL
        //userID = user.getUid();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_creation, container, false);
        createButton = (Button) view.findViewById(R.id.createClassButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                className = (EditText) view.findViewById(R.id.classNameField);
                instructorName = (EditText) view.findViewById(R.id.instructorField);
                classDescription = (EditText) view.findViewById(R.id.classDescriptionField);

                try {
                    if (className.getText().toString().isEmpty() || instructorName.getText().toString().isEmpty()
                            || classDescription.getText().toString().isEmpty()) {
                        //Shows a message if any of the fields are empty
                        Toast.makeText(getActivity(), "Field(s) cannot be left blank.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //Gets the name, instructor, and descriptions as Strings
                        String name = className.getText().toString();
                        String instructor = instructorName.getText().toString();
                        String description = classDescription.getText().toString();

                        ClassInfo tempClass = new ClassInfo(name, instructor, description);

                      /*  To Do:
                        Need user authorization to get userID

                        dbRef.child("Class").child(userID).setValue(tempClass);
                        */

                        if (listener != null) {
                            listener.changeFragment(1);
                        }


                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity a = new Activity();

        if (context instanceof Activity) {
            a = (Activity) context;
        }
        try {
            listener = (OnFragmentInteractionListener) a;
        } catch (ClassCastException e) {
            throw new ClassCastException(a.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;

    }

}