package com.example.rush.View.fragments.groups;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.rush.MainActivity;
import com.example.rush.Model.GroupInfo;
import com.example.rush.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


public class GroupCreationFragment extends Fragment {

    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    TextInputLayout groupInput, instructorInput, descriptionInput;
    TextInputEditText groupField, instructorName, groupDescription;
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
        View view = inflater.inflate(R.layout.fragment_group_creation, container, false);
        createButton = (Button) view.findViewById(R.id.createGroupButton);
        groupInput = view.findViewById(R.id.GroupName);
        instructorInput = view.findViewById(R.id.InstructorName);
        descriptionInput = view.findViewById(R.id.GroupDescription);
        groupField = view.findViewById(R.id.Name);
        instructorName = view.findViewById(R.id.Instructor);
        groupDescription = view.findViewById(R.id.Description);
        /*
            Erase any text input when the icon is clicked
         */
        groupInput.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                groupField.setText("");
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
                groupDescription.setText("");
            }
        });

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (groupField.getText().toString().isEmpty() || instructorName.getText().toString().isEmpty()
                            || groupDescription.getText().toString().isEmpty()) {
                        //Shows a message if any of the fields are empty
                        Toast.makeText(getActivity(), "Field(s) cannot be left blank.",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        //Gets the name, instructor, and descriptions as Strings
                        String groupName = groupField.getText().toString();
                        String instructor = instructorName.getText().toString();
                        String description = groupDescription.getText().toString();

                        if (userID != null) {
                            //Get the randomly generated document id
                           // String docID = database.collection("groups").document().getId();
                            GroupInfo tempGroup = new GroupInfo (groupName, instructor, description, userID );
                            //Write the new group to the database
                            database.collection("groups").document()
                                    .set(tempGroup);
                            //Cast the current activity to MainActivity to call method
                            ((MainActivity) getActivity()).groupsFragment();
                            //Show success message to user
                            Toast.makeText(getActivity(), "Group created!", Toast.LENGTH_SHORT).show();
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