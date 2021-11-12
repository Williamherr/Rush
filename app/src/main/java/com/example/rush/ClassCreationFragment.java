package com.example.rush;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClassCreationFragment extends Fragment {
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    TextInputLayout classInput, instructorInput, descriptionInput;
    TextInputEditText classField, instructorName, classDescription;
    String userID;
    Button createButton;

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
        View view = inflater.inflate(R.layout.fragment_class_creation, container, false);
        createButton = (Button) view.findViewById(R.id.createClassButton);
        classInput = view.findViewById(R.id.ClassName);
        instructorInput = view.findViewById(R.id.InstructorName);
        descriptionInput = view.findViewById(R.id.ClassDescription);
        classField = view.findViewById(R.id.Name);
        instructorName = view.findViewById(R.id.Instructor);
        classDescription = view.findViewById(R.id.Description);
        /*
            Erase any text input when the icon is clicked
         */
        classInput.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classField.setText("");
            }
        });
        instructorInput.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                instructorName.setText("");
            }
        });
        descriptionInput.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classDescription.setText("");
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (classField.getText().toString().isEmpty() || instructorName.getText().toString().isEmpty()
                            || classDescription.getText().toString().isEmpty()) {
                        //Shows a message if any of the fields are empty
                        Toast.makeText(getActivity(), "Field(s) cannot be left blank.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //Gets the name, instructor, and descriptions as Strings
                        String className = classField.getText().toString();
                        String instructor = instructorName.getText().toString();
                        String description = classDescription.getText().toString();

                        if (userID != null) {
                            ClassInfo tempClass = new ClassInfo(className, instructor, description, userID);
                            //Write the new class to the database
                            database.collection("classes").document()
                                    .set(tempClass);

                            //Cast the current activity to MainActivity to call method
                            ((MainActivity) getActivity()).classesFragment();
                            //Show success message to user
                            Toast.makeText(getActivity(), "Class created!", Toast.LENGTH_SHORT).show();
                        }


                    }
                } catch (NullPointerException e) {
                    Toast.makeText(getActivity(), "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        return view;

    }


}