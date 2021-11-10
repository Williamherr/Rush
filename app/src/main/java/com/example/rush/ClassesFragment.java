package com.example.rush;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ClassesFragment extends Fragment {

    FloatingActionButton fabButton;
    ClassDetailFragmentListener listener;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private RecyclerView.Adapter adapter;
    private ArrayList<ClassInfo> listOfClasses = new ArrayList<>();
    String userID;

    public interface ClassDetailFragmentListener {
        void goToClassDetails(String name, String instructor, String description);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (ClassesFragment.ClassDetailFragmentListener) context;
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
        //Button for opening the bottom dialog
        fabButton = view.findViewById(R.id.classOptionsButton);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show the bottom dialog when user clicks on button
                showDialog();
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
                                    ClassInfo obj = document.toObject(ClassInfo.class);
                                    //Keep track of all classes created by this user
                                    listOfClasses.add(obj);
                                    adapter = new ClassesFragment.ClassAdapter(listOfClasses);
                                    recycle.setAdapter(adapter);

                                }
                            } else {
                                Log.d("Error", "Error getting documents: ", task.getException());
                            }
                        }
                    });


        }


        return view;
    }

    private void showDialog() {
        BottomSheetDialog bottom = new BottomSheetDialog(getActivity());
        bottom.setContentView(R.layout.fragment_classes_bottom_dialog);
        LinearLayout newClasses = bottom.findViewById(R.id.newClasses);
        LinearLayout deleteClasses = bottom.findViewById(R.id.deleteClasses);

        newClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take the user to the class creation page
                ((MainActivity)getActivity()).creationFragment();
                bottom.dismiss();
            }
        });

        bottom.show();
    }

    /*
           ClassAdapter for the RecyclerView to list all classes users has created/joined
     */

    public class ClassAdapter extends RecyclerView.Adapter<com.example.rush.ClassesFragment.ClassAdapter.ViewHolder> {
        private ArrayList<ClassInfo> classList;

        public ClassAdapter(ArrayList<ClassInfo> classList) {
            this.classList = classList;
        }

        @NonNull
        @Override
        public com.example.rush.ClassesFragment.ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.class_items,
                    parent, false);
            return new com.example.rush.ClassesFragment.ClassAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull com.example.rush.ClassesFragment.ClassAdapter.ViewHolder holder, int position) {
            ClassInfo classObj = classList.get(position);
            SpannableString stringSpanner = new SpannableString(classObj.getClassName());
            stringSpanner.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner.length(), 0);
            String twoChars = classObj.getClassName().substring(0, 2).toUpperCase();


            holder.className.setText(stringSpanner);
            holder.classDescription.setText(classObj.getDescription());
            holder.identifier.setText(twoChars);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.goToClassDetails(classObj.getClassName(), classObj.getInstructor(),
                            classObj.getDescription());
                }
            });
        }

        @Override
        public int getItemCount() {
            if (classList != null) {
                return classList.size();
            } else {
                return 0;
            }
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View view;
            public TextView className;
            public TextView identifier;
            public TextView classDescription;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                className = view.findViewById(R.id.classNameText);
                classDescription = view.findViewById(R.id.classDescriptionText);
                identifier = view.findViewById(R.id.classIdentifier);
            }
        }
    }

}