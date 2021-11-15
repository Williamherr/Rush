package com.example.rush;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.content.Context;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;


public class LoginFragment extends Fragment {
    Button createAccount;
    CreateFragmentListener cListener;
    private EditText inputEmail, inputPassword;
    private Button btnLogIn, btnCreateAccount;
    private FirebaseAuth mAuth;
    final String TAG = "LoginFragment";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        inputEmail = view.findViewById(R.id.email);
        inputPassword = view.findViewById(R.id.password);
        createAccount = (Button) view.findViewById(R.id.goToAccountCreation);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        createAccount.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                cListener.goToAccountCreationFragment();
            }
        });
        btnLogIn = (Button) view.findViewById(R.id.login);
        btnLogIn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (validate()) {
                    String email = inputEmail.getText().toString();
                    String password = inputPassword.getText().toString();
                    mAuth = FirebaseAuth.getInstance();
                    signIn(email, password);
                }
            }
        });
        return view;


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cListener = (CreateFragmentListener) context;
    }

    public interface CreateFragmentListener {
        void goToAccountCreationFragment();

        void gotoHomeFragment(FirebaseUser uid);
    }

    public Boolean validate() {
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        if (email.replaceAll("\\s", "").isEmpty()) {
            Toast.makeText(getActivity(), "Email address cannot be empty", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
        if (password.isEmpty()) {
            Toast.makeText(getActivity(), "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public void signIn(String email, String password) {


        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user.getDisplayName() == null) {
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName("Fake Name").build();
                                user.updateProfile(profileUpdates);
                            }
                            cListener.gotoHomeFragment(user);
                            Log.d(TAG, user.getUid());

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Wrong Email or Password", Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }
                });
    }


}

