package com.example.rush.messages;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rush.R;


import com.example.rush.messages.Adapters.AllPrivateMessageAdapter;
import com.example.rush.messages.model.Members;
import com.example.rush.messages.model.Member;
import com.example.rush.messages.model.MessageList;
import com.example.rush.messages.model.Messages;
import com.example.rush.messages.model.PrivateMessageList;

import com.example.rush.messages.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.type.DateTime;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MessageFragment extends Fragment implements bottomSheetDialogFragment.IBottomSheetDialog, AllPrivateMessageAdapter.IMessageFragmentInterface, CreatePrivateMessages.iCreatePrivateMessages {

    public MessageFragment() {
        // Required empty public constructor
    }

    FloatingActionButton fabButton;
    ExtendedFloatingActionButton deleteFAB;
    Button cancel, submit;

    ArrayList<PrivateMessageList> PrivateMessageList;

    LinearLayoutManager layoutManager;
    MessageFragmentListener mListener;
    AllPrivateMessageAdapter adapter;
    private String uid;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");
    private final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    RecyclerView recyclerView;
    final String TAG = "MessageFragment";
    private MessageList messageList;
    private ArrayList<String> mid;
    Members members;
    private ArrayList<MessageList> allMessageList;
    private Messages recentMessage;
    FirebaseUser user;
    private ArrayList<MessageList> editIdList;
    private boolean isEdited = false;
    private boolean isDeleted = false;
    private ArrayList<String> urgentID;
    private String option = "";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessageFragmentListener) context;
    }


    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_message, container, false);
        recyclerView = view.findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        PrivateMessageList = new ArrayList<>();
        allMessageList = new ArrayList<>();
        urgentID = new ArrayList<>();
        cancel = view.findViewById(R.id.cancel);
        submit = view.findViewById(R.id.submit);
        user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // User is signed in
            uid = user.getUid();
        } else {
            // No user is signed in
        }
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fabButton.isShown())
                    fabButton.hide();
                if (dy > 0 || dy < 0 && fabButton.isShown() && isEdited == true) {
                    submit.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isEdited == false)
                    fabButton.show();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isEdited == true) {
                    submit.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        fabButton = view.findViewById(R.id.messageMenuButton);
        fabButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetButtons();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetsSubmit(option);

            }
        });

        showRecyclerList();


        return view;
    }

    /*  Bottom Sheet Menu
          - New Messages
          - Delete Messages
          - Notification
          - Mark Text as Urgent
          - Resolve Urgent Messages

     */

    // Bottom Sheet Dialog
    void showDialog() {
        // Create the fragment and show it as a dialog.
        BottomSheetDialogFragment bottom = bottomSheetDialogFragment.newInstance(this);
        bottom.show(getParentFragmentManager(), "dialog");
    }


    // Bottom Sheet Click
    // This method will pass a string into a interface base on the what item was clicked
    @Override
    public void sheetClicked(String string) {
        option = string;
        if (string == "new") {
            mListener.createNewMessages(this);
        } else if (string == "notification") {
            mListener.createNotifications();
        } else {
            editList();
        }

    }

    // Submit Button on Click
    public void bottomSheetsSubmit(String option) {
        if (option == "delete") {
            deleteMessages(editIdList);
        } else if (option == "urgent") {
            editUrgentMessages(true);
        } else if (option == "resolve") {
            editUrgentMessages(false);
        }

        resetButtons();
    }

    public void resetButtons() {
        submit.setVisibility(View.INVISIBLE);
        cancel.setVisibility(View.INVISIBLE);
        isEdited = false;
        fabButton.show();
        showRecycler();
    }


    // if an Item was clicked, then the list will be in edit mode
    public void editList() {
        isEdited = true;
        fabButton.hide();
        submit.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        editIdList = new ArrayList<>();
        adapter = new AllPrivateMessageAdapter(allMessageList, true,this);
        adapter = new AllPrivateMessageAdapter(allMessageList, true, this);
        recyclerView.setAdapter(adapter);
    }





    /*
        New Messages
     */

    // Creates new messages
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void createNewMessages(User otherUser) {
        // Update the user's messages id

        Map<String, Object> data = new HashMap<>();

        ArrayList<Map<String, Object>> members = new ArrayList<>();
        members.add(Map.of("uid", uid, "name", user.getDisplayName()));
        members.add(Map.of("uid", otherUser.getId(), "name", otherUser.getName()));
        data.put("members", members);
        data.put("time", Timestamp.now());
        data.put("recentMessage", "");

        String newDoc = messageRef.document().getId();
        messageRef.document(newDoc).set(data);

        db.collection("users").document(otherUser.getId()).update("messages", FieldValue.arrayUnion(newDoc));
        db.collection("users").document(uid).update("messages", FieldValue.arrayUnion(newDoc));


        mListener.goToPrivateChatFragment(otherUser.getName(), otherUser.getId(), newDoc);
    }


    /*

         Delete Message Section

     */
    // Deletes the Message Collection in all-private-messages
    private void deleteCollection(final CollectionReference collection, Executor executor) {
        Tasks.call(executor, () -> {
            int batchSize = 10;
            Query query = collection.orderBy(FieldPath.documentId()).limit(batchSize);
            List<DocumentSnapshot> deleted = deleteQueryBatch(query);

            while (deleted.size() >= batchSize) {
                DocumentSnapshot last = deleted.get(deleted.size() - 1);
                query = collection.orderBy(FieldPath.documentId()).startAfter(last.getId()).limit(batchSize);

                deleted = deleteQueryBatch(query);
            }

            return null;
        });
    }

    //The batch + query for the deleteCollection
    @WorkerThread
    private List<DocumentSnapshot> deleteQueryBatch(final Query query) throws Exception {
        QuerySnapshot querySnapshot = Tasks.await(query.get());

        WriteBatch batch = query.getFirestore().batch();
        for (DocumentSnapshot snapshot : querySnapshot) {
            batch.delete(snapshot.getReference());
        }
        Tasks.await(batch.commit());

        return querySnapshot.getDocuments();
    }

    // Delete the message id inside the user and other user class
    public void deleteMessages(ArrayList<MessageList> list) {

        for (int i = 0; i < list.size(); i++) {
            String mid = list.get(i).getKey();
            String otherUID = list.get(i).getMembers().getOtherMember(uid);
            Log.d(TAG, "deleteMessages: " + otherUID);
            db.collection("users").document(uid).update("messages", FieldValue.arrayRemove(mid));
            db.collection("users").document(otherUID).update("messages", FieldValue.arrayRemove(mid));
            deleteCollection(messageRef.document(mid).collection("messages"), Executors.newSingleThreadExecutor());
            messageRef.document(mid).delete();

        }


    }

    // Delete messages interface
    @Override
    public void deleteMessages(Boolean isChecked, MessageList messageList) {
        if (isChecked) {
            editIdList.add(messageList);
        }else {
            editIdList.remove(messageList);
        }
    }




    /*

      Urgent Messages

    */

    public void editUrgentMessages(Boolean isUrgent) {
        Map<String, Object> data = new HashMap<>();
        data.put("isUrgent", isUrgent);

        for (int i = 0; i < editIdList.size(); i++) {
            String mid = editIdList.get(i).getKey();
            messageRef.document(mid).update(data);
            if (!isUrgent) {
                // This will make all urgent messages inside a chat false
                messageRef.document(mid).collection("messages").whereEqualTo("isUrgent", true).get()
                        .addOnSuccessListener( new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot value) {

                                for (DocumentSnapshot doc : value) {
                                    messageRef.document(mid).collection("messages")
                                            .document(doc.getId())
                                            .update("isUrgent", false);
                                }
                            }
                        });
            }
        }

    }

    // Mark as Urgent Interface
    @Override
    public void markUrgentMessages(Boolean isChecked, String mid) {
        if (isChecked) {
            urgentID.add(mid);
        } else {
            urgentID.remove(mid);
        }
    }


    // Initial the values for the recycler view / List of users
    public void showRecyclerList() {

        // Finds the user
        db.collection("users").document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable  DocumentSnapshot value, @Nullable  FirebaseFirestoreException error) {
                Log.d(TAG, "Adding User" );
                allMessageList = new ArrayList<>();
                mid = (ArrayList<String>) value.getData().get("messages");
                if (!mid.isEmpty()) {
                    list();
                }
            }

        });

    }
    public void list() {
        for (String id : mid) {

            // Finds the document for the id
            messageRef.document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable  DocumentSnapshot doc, @Nullable  FirebaseFirestoreException error) {

                    Log.d(TAG, "DOC " + doc.getId());
                    Log.d(TAG, "Id " + id);
                    // Checks to see if the document already exist in the arraylist
                    for (int i = 0; i < allMessageList.size(); i++) {

                        if (id == allMessageList.get(i).getKey()) {
                            allMessageList.remove(i);
                        }
                    }

                    Log.d(TAG, "" + allMessageList.size());

                    Boolean isUrgent = (Boolean) doc.getBoolean("isUrgent");
                    String message = (String) doc.get("recentMessage");
                    Timestamp time = (Timestamp) doc.get("time");
                    members = new Members();
                    recentMessage = new Messages();
                    recentMessage.setMessage(message);
                    recentMessage.setTime(time);

                    if (isUrgent == null) {
                        isUrgent = false;
                    }
                    recentMessage.setIsUrgent(isUrgent);

                    ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) doc.get("members");

                    // Initializing variable for user list view
                    try {
                        for (Map<String, Object> member : users) {
                            Member mem = new Member(member.get("name").toString(), member.get("uid").toString());
                            members.addMembers(mem);
                        }
                        messageList = new MessageList(members, recentMessage, id);
                        allMessageList.add(messageList);

                    }
                    catch (Exception e) {
                    }

                    showRecycler();

                }
            });

        }
    }



    // sets the adapter and shows the recycler view
    void showRecycler() {
        // Sorts allMessageList by firebase.time
        Collections.sort(allMessageList, new Comparator<MessageList>() {
            @Override
            public int compare(MessageList m1, MessageList m2) {
                try {
                    Timestamp time1 = m1.getMessages().getTime();
                    Timestamp time2 = m2.getMessages().getTime();
                    if (time1.compareTo(time2) > 0) {
                        return -1;
                    } else if (time1.compareTo(time2) < 0) {
                        return 1;
                    } else {
                        return time1.compareTo(time2);
                    }

                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });


        adapter = new AllPrivateMessageAdapter(allMessageList, this);
        recyclerView.setAdapter(adapter);
    }



   /*

      Interfaces

    */

    public interface MessageFragmentListener {
        void goToPrivateChatFragment(String otherUserName, String otherUserId, String messageKey);

        void createNewMessages(CreatePrivateMessages.iCreatePrivateMessages iListener);

        void createNotifications();
    }

    @Override
    public void goToPrivateChatFrag(String otherUserName, String otherUID, String messageKey) {
        mListener.goToPrivateChatFragment(otherUserName, otherUID, messageKey);
    }


}