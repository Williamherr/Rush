package com.example.rush;

import android.os.Bundle;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import android.widget.Button;
import android.widget.EditText;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioButton;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.api.LogDescriptor;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;


public class AccountCreationFragment extends Fragment {
    Button btnCreate;
    EditText inputLName, inputPassword, inputCPassword, inputEmail;
    RadioGroup radiogroup;
    TextInputEditText inputFName;
    String userID;
    private FirebaseAuth mAuth;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    AccountCreationFragmentListener mListener;
    String type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_account_creation, container, false);
        inputFName = view.findViewById(R.id.firstName);
        inputLName = view.findViewById(R.id.lastName);
        inputEmail = view.findViewById(R.id.newemail);
        inputPassword = view.findViewById(R.id.newpassword);
        inputCPassword = view.findViewById(R.id.confirmPassword);
        btnCreate = view.findViewById(R.id.createAccountButton);
        radiogroup = view.findViewById(R.id.radiogroup);

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {

                if (id == R.id.professor) {
                    type = "Professor";

                } else if (id == R.id.student) {
                    type = "Student";
                }
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fName = inputFName.getText().toString();
                String lName = inputLName.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                String confirmPassword = inputCPassword.getText().toString();
                if (validate()) {
                    createAccount(fName, lName, email, password, type);
                }

            }
        });


        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (AccountCreationFragmentListener) context;
    }

    public interface AccountCreationFragmentListener {
        void gotoHomeFragment(String uid);
    }

    public Boolean validate() {
        String fName = inputFName.getText().toString();
        String lName = inputLName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String confirmPassword = inputCPassword.getText().toString();
        if (TextUtils.isEmpty(fName)) {
            Toast.makeText(getActivity(), "First name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(lName)) {
            Toast.makeText(getActivity(), "Last name cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(getActivity(), "Confirm password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(getActivity(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void createAccount(String fName, String lName, String email, String password, String type) {
        String Tag = "Register";
        mAuth = FirebaseAuth.getInstance();
        Log.d(Tag, fName + lName + email + password + type);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(Tag, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            userID = user.getUid();
                            if (user.getDisplayName() == null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(fName + " " + lName).build();
                                user.updateProfile(profileUpdates);
                            }
                            Map<String, Object> useracc = new HashMap<>();
                            useracc.put("fname", fName);
                            useracc.put("lname", lName);
                            useracc.put("type", type);
                            useracc.put("name", fName + " " + lName);
                            db.collection("users").document(userID).set(useracc);
                            mListener.gotoHomeFragment(user.getUid());

                        } else {
                            Log.w(Tag, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

}