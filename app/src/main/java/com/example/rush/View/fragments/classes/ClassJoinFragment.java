package com.example.rush.View.fragments.classes;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.rush.MainActivity;
import com.example.rush.Model.ClassInfo;
import com.example.rush.Model.Member;
import com.example.rush.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ClassJoinFragment extends Fragment {
    private TextInputLayout codeLayout;
    private TextInputEditText joinCode;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String userID;
    private Button joinBtn;
    private String userInput;


    public ClassJoinFragment() {
    }


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
        View view = inflater.inflate(R.layout.fragment_class_join, container, false);
        codeLayout = view.findViewById(R.id.joinCodeLayout);
        joinCode = view.findViewById(R.id.joinCode);
        joinBtn = view.findViewById(R.id.joinButton);

        codeLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                joinCode.setText("");
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userInput = joinCode.getText().toString();
                if (userInput.isEmpty()) {
                    Toast.makeText(getActivity(), "Code cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> student = new HashMap<>();
                student.put("Name", user.getDisplayName());
                student.put("ID", userID);
                student.put("Email", user.getEmail());
                database.collection("classes").whereEqualTo("classID", userInput).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //Give an error message if no classes found
                                    if (task.getResult().isEmpty()) {
                                        Toast.makeText(getActivity(), "No class found with that code", Toast.LENGTH_SHORT).show();
                                    //Means a class was found with the join code
                                    } else {
                                        Toast.makeText(getActivity(), "Class joined!", Toast.LENGTH_SHORT).show();
                                        //Add the current student to the class based on the join code
                                        database.collection("classes").document(userInput).collection("Students")
                                                .document(userID).set(student);
                                        //Go back to the list of user's classes
                                        ((MainActivity) getActivity()).classesFragment();
                                    }

                                }
                            }
                        });

            }
        });

        return view;
    }


}
