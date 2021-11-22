package com.example.rush.View.fragments.messages;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.example.rush.R;
import com.example.rush.View.adapters.messages.SearchUserAdapter;
import com.example.rush.Model.Member;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class CreatePrivateMessages extends Fragment implements SearchUserAdapter.ISearchUserInterface {

    String TAG = "CreateNewPrivateMessages";
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");



    public CreatePrivateMessages() {
        // Required empty public constructor
    }

    public CreatePrivateMessages(iCreatePrivateMessages iListener) {
        // Required empty public constructor
        this.iListener = iListener;
    }


    TextInputLayout textInputLayout;
    TextInputEditText toText;
    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    SearchUserAdapter searchUserAdapter;
    iCreatePrivateMessages iListener;
    ArrayList<Member> users;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_create_private_messages, container, false);


        toText = view.findViewById(R.id.toText);
        users = new ArrayList<>();
        recyclerView = view.findViewById(R.id.recyclerViewFindUsers);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        textInputLayout = view.findViewById(R.id.toUser);
        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toText.setText("");
            }
        });

        toText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String search = charSequence.toString().trim();
                if (charSequence.length() > 0) {
                    search = capitalizeWord(search);
                    Log.d(TAG, "onTextChanged: " + search);
                }
                searchUsers(search);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });



        return view;
    }

    public String capitalizeWord(String str){
        String words[]=str.split("\\s");
        String capitalizeWord="";
        for(String w:words){
            String first = w.substring(0,1);
            String next = w.substring(1);
            capitalizeWord += first.toUpperCase() + next + " ";
        }
        return capitalizeWord.trim();
    }

    public void searchUsers(String name) {
        db.collection("users").orderBy("name").startAt(name).endAt(name + "\uf8ff").get().addOnSuccessListener( new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                Log.d(TAG, "Snaps: " + queryDocumentSnapshots.size());
                users.clear();
                for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                    Member user = new Member(doc.get("name").toString(), doc.getId());
                    Log.d(TAG, "onSuccess: " + doc.getId());
                    users.add(user);
                }
                showRecycler();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull  Exception e) {
                Log.d(TAG, "onFailure: " + e);
            }
        });
    }

    public void showRecycler() {
        searchUserAdapter = new SearchUserAdapter(users,this);
        recyclerView.setAdapter(searchUserAdapter);
    }


    @Override
    public void createNewMessage(Member user) {
        getParentFragmentManager().popBackStack();

        iListener.createNewMessages(user);
    }

    public interface iCreatePrivateMessages{
        void createNewMessages(Member user);
    }
}