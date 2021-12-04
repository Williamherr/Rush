package com.example.rush.View.fragments.groups;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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
import android.widget.Toast;

import com.example.rush.MainActivity;
import com.example.rush.Model.GroupInfo;
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


public class GroupsFragment extends Fragment {

    private FloatingActionButton fabButton;
    private ExtendedFloatingActionButton deleteBtn, cancelBtn;
    private GroupsFragment.GroupDetailFragmentListener listener;
    private String userID;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private String type = "";
    private GroupsFragment.GroupAdapter adapter;
    private RecyclerView recycle;
    private ArrayList<GroupInfo> listOfGroups = new ArrayList<>();
    private ArrayList<GroupInfo> groupsToDelete = new ArrayList<>();

    public interface GroupDetailFragmentListener {
        void goToGroupDetails(String name, String instructor, String description, String id, String createdBy);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (GroupsFragment.GroupDetailFragmentListener) context;
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
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        /*
            This section initializes the RecyclerView to show the user's groupes
         */
        recycle = (RecyclerView) view.findViewById(R.id.groups);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recycle.setLayoutManager(manager);
        recycle.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        //Button for opening the bottom dialog
        fabButton = view.findViewById(R.id.groupOptionsButton);
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
                builder.setTitle("Warning!");
               // if (type.equals("Professor")) {
                  //  messageContext = "Classes cannot be recovered once deleted! Are you sure you want to delete?";
              //  } else {
                //    messageContext = "Are you sure you want to leave the selected class(es)?";
               // }
                builder.setMessage(messageContext);
                builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < groupsToDelete.size(); j++) {
                            //Remove each deleted class from the list of classes
                            listOfGroups.remove(groupsToDelete.get(j));
                            database.collection("groups")
                                    //Find the document in the database by the classes docID and delete it
                                    .document(groupsToDelete.get(j).getDocID()).delete()
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
                            getGroups(type);
                        }
                    });

        }


        return view;
    }

    private void getGroups(String s) {
      //  if (s.equals("Professor")) {
            database.collection("groups")
                    //Get any groups created by the current professor
                    .whereEqualTo("createdBy", userID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Success", document.getId() + " => " + document.getData());
                                    //Cast the QueryDocumentSnapshot into a GroupInfo obj
                                    GroupInfo obj = document.toObject(GroupInfo.class);
                                    obj.setDocID(document.getId());
                                    //Keep track of all groups created by this user
                                    listOfGroups.add(obj);
                                    adapter = new GroupsFragment.GroupAdapter(listOfGroups);
                                    recycle.setAdapter(adapter);

                                }
                            } else {
                                Log.d("Error", "Error getting documents: ", task.getException());
                           }
                        }
                    });
     //   } else {
            //This should get all groups the current student has joined
        }
   // }





    private void showDialog() {
        BottomSheetDialog bottom = new BottomSheetDialog(getActivity());
        bottom.setContentView(R.layout.fragment_groups_bottom_dialog);
        LinearLayout newGroups = bottom.findViewById(R.id.newGroups);
        LinearLayout deleteGroups = bottom.findViewById(R.id.deleteGroups);

        newGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Take the user to the Group creation page
            //    if (type.equals("Professor")) {
                    ((MainActivity) getActivity()).createFragment();
              //  }else {
                    //Go to the join Group tab since students can't create a class
             //   }
                Log.d("TAG", "onClick: ");
             //  bottom.dismiss();

            }
        });
        deleteGroups.setOnClickListener(new View.OnClickListener() {
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
           GroupAdapter for the RecyclerView to list all groups users has created/joined
     */

    public class GroupAdapter extends RecyclerView.Adapter<GroupsFragment.GroupAdapter.ViewHolder> {
        private ArrayList<GroupInfo> groupList;
        private boolean isDeleting;

        public GroupAdapter(ArrayList<GroupInfo> groupList) {
            this.groupList = groupList;
        }

        public void setDeletionStatus(boolean b) {
            isDeleting = b;
        }

        @NonNull
        @Override
        public GroupsFragment.GroupAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.group_items,
                    parent, false);
            return new GroupsFragment.GroupAdapter.ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupsFragment.GroupAdapter.ViewHolder holder, int position) {
            GroupInfo groupObj = groupList.get(position);
            SpannableString stringSpanner = new SpannableString(groupObj.getGroupName());
            stringSpanner.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner.length(), 0);
            String twoChars = groupObj.getGroupName().substring(0, 2).toUpperCase();

            if (isDeleting) {
                //Show boxes only if user is deleting groups
                holder.box.setVisibility(View.VISIBLE);
            } else {
                //Boxes should disappear when not in use
                holder.box.setVisibility(View.GONE);
            }
            /*
                Get any groups that have been selected for deletion
             */
            holder.box.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean isChecked = holder.box.isChecked();
                    if (isChecked) {
                        //Add current checked object to list of groupes to delete
                        groupsToDelete.add(groupObj);
                    } else {
                        //Remove current group from groups to delete if not checked
                        groupsToDelete.remove(groupObj);
                    }
                }
            });
            holder.groupName.setText(stringSpanner);
            holder.groupDescription.setText(groupObj.getDescription());
            holder.identifier.setText(twoChars);
        }

        @Override
        public int getItemCount() {
            if (groupList != null) {
                return groupList.size();
            } else {
                return 0;
            }
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private TextView groupName;
            private TextView identifier;
            private TextView groupDescription;
            private CheckBox box;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                groupName = view.findViewById(R.id.groupNameText);
                groupDescription = view.findViewById(R.id.groupDescriptionText);
                identifier = view.findViewById(R.id.groupIdentifier);
                box = view.findViewById(R.id.deleteBox);
            }
        }
    }

}