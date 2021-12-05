package com.example.rush.View.fragments.classes;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rush.MainActivity;
import com.example.rush.Model.ClassInfo;
import com.example.rush.Model.Messages;
import com.example.rush.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.zip.Inflater;


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
    private ActionBar actionBar;

    public interface ClassDetailFragmentListener {
        void goToClassDetails(String name, String instructor, String description, String id, String createdBy);

        void goToClassChat(String id, String name);
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

    //Sorts classes alphabetically by name
    private void sortClasses() {
        Collections.sort(listOfClasses, new Comparator<ClassInfo>() {
            @Override
            public int compare(ClassInfo c1, ClassInfo c2) {
                int comparison = c1.getClassName().compareTo(c2.getClassName());

                if (comparison < 0) {
                    return -1;
                } else if (comparison == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });
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
        actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle("Classes");
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
                    //Set the adapter deletion status for boxes to disappear
                    adapter.setDeletionStatus(false);
                    recycle.setAdapter(adapter);
                }
                //Hide the cancel and delete buttons
                fabButton.show();
                deleteBtn.hide();
                cancelBtn.hide();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String messageContext = "";
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(true);
                builder.setTitle("Warning");
                builder.setIcon(R.drawable.ic_warning);
                if (type.equals("Professor")) {
                    messageContext = "Classes cannot be recovered once deleted! Are you sure you want to delete?";
                } else {
                    messageContext = "Are you sure you want to leave the selected class(es)?";
                }
                builder.setMessage(messageContext);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < classesToDelete.size(); j++) {
                            //Get reference to documents being deleted
                            DocumentReference docRef = database.collection("classes")
                                    .document(classesToDelete.get(j).getClassID());
                            //Remove each deleted class from the list of classes
                            listOfClasses.remove(classesToDelete.get(j));
                            if (type.equals("Professor")) {
                                docRef.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Success", "DocumentSnapshot successfully deleted!");
                                                dialogInterface.dismiss();
                                                //No longer deleting, so hide the delete and cancel buttons
                                                adapter.setDeletionStatus(false);
                                                recycle.setAdapter(adapter);
                                                fabButton.show();
                                                deleteBtn.hide();
                                                cancelBtn.hide();
                                            }
                                        });
                            } else {
                                //Students can leave the class instead of the entire class being deleted
                                DocumentReference studentRef = docRef.collection("Students")
                                        .document(userID);
                                studentRef.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Log.d("Success", "DocumentSnapshot successfully deleted!");
                                                dialogInterface.dismiss();
                                                //No longer deleting, so hide the delete and cancel buttons
                                                adapter.setDeletionStatus(false);
                                                recycle.setAdapter(adapter);
                                                fabButton.show();
                                                deleteBtn.hide();
                                                cancelBtn.hide();
                                            }
                                        });
                            }

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

                            if (type == null) {
                                type = "Student";
                            }
                            getClasses(type);
                        }
                    });

        }


        return view;
    }

    private void getClasses(String s) {

        if (s.equals("Professor")) {
            database.collection("classes")
                    //Get any classes created by the current professor
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
                                    sortClasses();
                                    adapter = new ClassesFragment.ClassAdapter(listOfClasses);
                                    recycle.setAdapter(adapter);

                                }
                            } else {
                                Log.d("Error", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {

            //Get all classes that have students
            database.collectionGroup("Students")
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                //Get the student id
                                String id = (String) document.getData().get("ID");
                                //Only need classes for the current student
                                if (userID.equals(id) && id != null) {
                                    //Backtrack to get the class itself
                                    DocumentReference parentRef = document.getReference().getParent().getParent();
                                    parentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                //Cast task result into a ClassInfo object
                                                ClassInfo obj = task.getResult().toObject(ClassInfo.class);
                                                //Add to list of classes
                                                listOfClasses.add(obj);
                                                sortClasses();
                                                adapter = new ClassesFragment.ClassAdapter(listOfClasses);
                                                recycle.setAdapter(adapter);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
            });
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
                    ((MainActivity) getActivity()).goToJoinFragment();
                }
                bottom.dismiss();

            }
        });
        deleteClasses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapter != null) {
                    //Now deleting so show checkboxes
                    adapter.setDeletionStatus(true);
                    recycle.setAdapter(adapter);
                }
                //Show the delete and cancel buttons
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
                //Show boxes only if user is deleting classes
                holder.box.setVisibility(View.VISIBLE);
            } else {
                //Boxes should disappear when not in use
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
                        //Add current checked object to list of classes to delete
                        classesToDelete.add(classObj);
                    } else {
                        //Remove current class from classes to delete if not checked
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
                    CharSequence[] options;
                    SpannableString[] formatOptions;
                    String name = classObj.getClassName();
                    String instructor = classObj.getInstructor();
                    String description = classObj.getDescription();
                    String id = classObj.getClassID();
                    String createdBy = classObj.getCreatedBy();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(true);
                    builder.setIcon(R.drawable.book);

                    if (type.equals("Professor")) {
                        builder.setTitle("Class invite code " + id);
                        options = new CharSequence[]{"Copy Code", "View Class", "Class Chat",
                                "Close"};
                        formatOptions = new SpannableString[options.length];
                        //Format the options text
                        for (int i = 0; i < formatOptions.length; i++) {
                            formatOptions[i] = new SpannableString(options[i]);
                            formatOptions[i].setSpan(new StyleSpan(Typeface.ITALIC), 0, formatOptions[i].length(), 0);
                            formatOptions[i].setSpan(new ForegroundColorSpan(Color.rgb(39, 109, 190)), 0, formatOptions[i].length(), 0);
                            //Make the lost option "Close" red
                            if (i == formatOptions.length - 1) {
                                formatOptions[i].setSpan(new StyleSpan(Typeface.ITALIC), 0, formatOptions[i].length(), 0);
                                formatOptions[i].setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, formatOptions[i].length(), 0);
                            }

                        }
                        builder.setItems(formatOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Class ID", classObj.getClassID());
                                        clipboard.setPrimaryClip(clip);
                                        Toast.makeText(getActivity(), "Link copied to clipboard", Toast.LENGTH_SHORT).show();
                                        dialogInterface.dismiss();
                                        break;
                                    case 1:
                                        listener.goToClassDetails(name, instructor, description, id, createdBy);
                                        listOfClasses.clear();
                                        dialogInterface.dismiss();
                                        break;
                                    case 2:
                                        listener.goToClassChat(id, name);
                                        listOfClasses.clear();
                                        dialogInterface.dismiss();
                                        break;
                                    case 3:
                                        dialogInterface.dismiss();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {
                        builder.setTitle(name);
                        options = new CharSequence[]{"View Class", "Class Chat", "Close"};
                        formatOptions = new SpannableString[options.length];
                        //Format the options text
                        for (int i = 0; i < formatOptions.length; i++) {
                            formatOptions[i] = new SpannableString(options[i]);
                            formatOptions[i].setSpan(new StyleSpan(Typeface.ITALIC), 0, formatOptions[i].length(), 0);
                            formatOptions[i].setSpan(new ForegroundColorSpan(Color.rgb(39, 109, 190)), 0, formatOptions[i].length(), 0);
                            //Make the lost option "Close" red
                            if (i == formatOptions.length - 1) {
                                formatOptions[i].setSpan(new StyleSpan(Typeface.ITALIC), 0, formatOptions[i].length(), 0);
                                formatOptions[i].setSpan(new ForegroundColorSpan(Color.rgb(255, 0, 0)), 0, formatOptions[i].length(), 0);
                            }
                        }
                        builder.setItems(formatOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        listener.goToClassDetails(name, instructor, description, id, createdBy);
                                        listOfClasses.clear();
                                        dialogInterface.dismiss();
                                        break;
                                    case 1:
                                        listener.goToClassChat(id, name);
                                        listOfClasses.clear();
                                        dialogInterface.dismiss();
                                        break;
                                    case 2:
                                        dialogInterface.dismiss();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
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
            private View view;
            private TextView className;
            private TextView identifier;
            private TextView classDescription;
            private CheckBox box;

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