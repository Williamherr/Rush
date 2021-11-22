package com.example.rush.View.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rush.MainActivity;
import com.example.rush.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class ClassesFragment extends Fragment {

    private FloatingActionButton fabButton;
    private ExtendedFloatingActionButton deleteBtn, cancelBtn;
    private ClassDetailFragmentListener listener;
    private String userID;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String type = "";
    private ClassAdapter adapter;
    private RecyclerView recycle;
    private ArrayList<ClassInfo> listOfClasses = new ArrayList<>();
    private ArrayList<ClassInfo> classesToDelete = new ArrayList<>();

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
        recycle = (RecyclerView) view.findViewById(R.id.classes);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recycle.setLayoutManager(manager);
        recycle.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        //Button for opening the bottom dialog
        fabButton = view.findViewById(R.id.classOptionsButton);
        deleteBtn = view.findViewById(R.id.deleteButton);
        cancelBtn = view.findViewById(R.id.cancelDelete);

        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Show the bottom dialog when user clicks on button
                showDialog();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null) {
                    adapter.setDeletionStatus(false);
                    recycle.setAdapter(adapter);
                }
                fabButton.show();
                deleteBtn.hide();
                cancelBtn.hide();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setTitle("Warning!");
                builder.setMessage("Classes cannot be recovered once deleted! Are you sure you want to delete?");
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < classesToDelete.size(); j++) {
                            //Remove each deleted class from the list of classes
                            listOfClasses.remove(classesToDelete.get(j));
                            database.collection("classes")
                                    .document(classesToDelete.get(j).getDocID()).delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Log.d("Success", "DocumentSnapshot successfully deleted!");
                                            dialogInterface.dismiss();
                                            adapter.setDeletionStatus(false);
                                            recycle.setAdapter(adapter);
                                            fabButton.show();
                                            deleteBtn.hide();
                                            cancelBtn.hide();

                                        }
                                    });
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        Log.d("Dismiss", "Cancel button was hit");
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        //Check if user is not null
        if (userID != null) {
            //Get the current user's document
            database.collection("users").document(userID).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            DocumentSnapshot document = task.getResult();
                            //Get the current user's account type
                            type = (String) document.getData().get("type");
                            getClasses(type);
                        }
                    });

        }


        return view;
    }

    private void getClasses(String s) {
        if (s.equals("Professor")) {
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
                                    obj.setDocID(document.getId());
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
        } else {
            Log.d("User", "For students");
        }
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
                if (type.equals("Professor")) {
                    ((MainActivity) getActivity()).creationFragment();
                } else {
                    //Go to the join class tab since students can't create a class
                }
                bottom.dismiss();

            }
        });
        deleteClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null) {
                    adapter.setDeletionStatus(true);
                    recycle.setAdapter(adapter);
                }
                fabButton.hide();
                deleteBtn.show();
                cancelBtn.show();
                bottom.dismiss();

            }
        });

        bottom.show();
    }

    /*
           ClassAdapter for the RecyclerView to list all classes users has created/joined
     */

    public class ClassAdapter extends RecyclerView.Adapter<ClassesFragment.ClassAdapter.ViewHolder> {
        private ArrayList<ClassInfo> classList;
        private boolean isDeleting;

        public ClassAdapter(ArrayList<ClassInfo> classList) {
            this.classList = classList;
        }

        public void setDeletionStatus(boolean b) {
            isDeleting = b;
        }

        @NonNull
        @Override
        public ClassesFragment.ClassAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.class_items,
                    parent, false);
            return new ClassesFragment.ClassAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ClassesFragment.ClassAdapter.ViewHolder holder, int position) {
            ClassInfo classObj = classList.get(position);
            SpannableString stringSpanner = new SpannableString(classObj.getClassName());
            stringSpanner.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner.length(), 0);
            String twoChars = classObj.getClassName().substring(0, 2).toUpperCase();

            if (isDeleting) {
                holder.box.setVisibility(View.VISIBLE);
            } else {
                holder.box.setVisibility(View.GONE);
            }
            /*
                Get any classes that have been selected for deletion
             */
            holder.box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = holder.box.isChecked();
                    if (isChecked) {
                        classesToDelete.add(classObj);
                        //Loop is for testing purposes
                        for (int i = 0; i < classesToDelete.size(); i++) {
                            Log.d("Debug", classesToDelete.get(i).getClassName());
                        }
                    } else {
                        classesToDelete.remove(classObj);
                    }
                }
            });
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
            public CheckBox box;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                className = view.findViewById(R.id.classNameText);
                classDescription = view.findViewById(R.id.classDescriptionText);
                identifier = view.findViewById(R.id.classIdentifier);
                box = view.findViewById(R.id.deleteBox);
            }
        }
    }

}