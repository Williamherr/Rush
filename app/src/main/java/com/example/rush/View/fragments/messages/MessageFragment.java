package com.example.rush.View.fragments.messages;


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


import com.example.rush.View.adapters.messages.AllPrivateMessageAdapter;
import com.example.rush.Model.Members;
import com.example.rush.Model.Member;
import com.example.rush.Model.MessageList;
import com.example.rush.Model.Messages;


import com.google.android.gms.tasks.Tasks;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;



public class MessageFragment extends Fragment implements bottomSheetDialogFragment.IBottomSheetDialog, AllPrivateMessageAdapter.IMessageFragmentInterface, CreatePrivateMessages.iCreatePrivateMessages {

    public MessageFragment() {
        // Required empty public constructor
    }
    FirebaseUser user;
    String uid;

    public MessageFragment(FirebaseUser user) {
        // Required empty public constructor
        this.user = user;
        this.uid = user.getUid();
    }

    FloatingActionButton fabButton;
    Button cancel, submit;

    LinearLayoutManager layoutManager;
    MessageFragmentListener mListener;
    AllPrivateMessageAdapter adapter;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    RecyclerView recyclerView;
    final String TAG = "MessageFragment";
    private ArrayList<String> mid;
    Members members;
    private ArrayList<MessageList> allMessageList;
    private Messages recentMessage;
    private ArrayList<MessageList> editIdList;
    private boolean isEdited = false;
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
        allMessageList = new ArrayList<>();
        cancel = view.findViewById(R.id.cancel);
        submit = view.findViewById(R.id.submit);



        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 || dy < 0 && fabButton.isShown())
                    fabButton.hide();
                if (dy > 0 || dy < 0 && fabButton.isShown() && isEdited) {
                    submit.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && !isEdited)
                    fabButton.show();
                if (newState == RecyclerView.SCROLL_STATE_IDLE && isEdited) {
                    submit.setVisibility(View.VISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        fabButton = view.findViewById(R.id.messageMenuButton);
        fabButton.setOnClickListener(view1 -> showDialog());

        cancel.setOnClickListener(view1 -> resetButtons());

        submit.setOnClickListener(view1 -> bottomSheetsSubmit(option));

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
        if (string.equals("new")) {
            mListener.createNewMessages(this);
        } else if (string.equals("notification")) {
            mListener.createNotifications();
        } else {
            editList();
        }

    }

    // Submit Button on Click
    public void bottomSheetsSubmit(String option) {
        switch (option) {
            case "delete":
                deleteMessages(editIdList);
                break;
            case "urgent":
                editUrgentMessages(true);
                break;
            case "resolve":
                editUrgentMessages(false);
                break;
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
        recyclerView.setAdapter(adapter);
    }





    /*
        New Messages
     */

    // Creates new messages
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void createNewMessages(Member otherUser) {
        // Update the user's messages id

        Map<String, Object> data = new HashMap<>();

        ArrayList<Map<String, Object>> members = new ArrayList<>();
        members.add(Map.of("uid", uid, "name", user.getDisplayName()));
        members.add(Map.of("uid", otherUser.getUid(), "name", otherUser.getName()));
        data.put("members", members);
        data.put("time", Timestamp.now());
        data.put("recentMessage", "");

        String newDoc = messageRef.document().getId();
        messageRef.document(newDoc).set(data);

        db.collection("users").document(otherUser.getUid()).update("messages", FieldValue.arrayUnion(newDoc));
        db.collection("users").document(uid).update("messages", FieldValue.arrayUnion(newDoc));


        mListener.goToPrivateChatFragment(otherUser, newDoc);
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
            String otherUID = list.get(i).getMembers().getOtherMember(uid).getUid();
            Log.d(TAG, "deleteMessages: " + otherUID);
            db.collection("users").document(uid).update("messages", FieldValue.arrayRemove(mid));
            db.collection("users").document(otherUID).update("messages", FieldValue.arrayRemove(mid));
            deleteCollection(messageRef.document(mid).collection("messages"), Executors.newSingleThreadExecutor());
            messageRef.document(mid).delete();

        }


    }

    // Delete messages interface
    @Override
    public void editMessages(Boolean isChecked, MessageList messageList) {
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

            if (!isUrgent) {
                // This will make all urgent messages inside a chat false
                messageRef.document(mid).collection("messages").whereEqualTo("isUrgent", true).get()
                        .addOnSuccessListener(value -> {
                            for (DocumentSnapshot doc : value) {
                                messageRef.document(mid).collection("messages")
                                        .document(doc.getId())
                                        .update("isUrgent", false);
                            }
                        });


            }else {
                messageRef.document(mid).update(data);
            }

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
                if (!mid.isEmpty() && mid != null) {
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
                        MessageList messageList = new MessageList(members, recentMessage, id);
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

        void createNewMessages(CreatePrivateMessages.iCreatePrivateMessages iListener);

        void createNotifications();

        void goToPrivateChatFragment(Member otherUser, String messageKey);
    }

    @Override
    public void goToPrivateChatFrag(Member otherUser, String messageKey) {
        mListener.goToPrivateChatFragment(otherUser, messageKey);
    }


}