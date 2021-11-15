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


public class MessageFragment extends Fragment implements bottomSheetDialogFragment.IBottomSheetDialog, AllPrivateMessageAdapter.IMessageFragmentInterface,CreatePrivateMessages.iCreatePrivateMessages {

    public MessageFragment() {
        // Required empty public constructor
    }

    FloatingActionButton fabButton;
    ExtendedFloatingActionButton deleteFAB;
    Button cancel, markAsUrgent;

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
    private ArrayList<MessageList> deleteMessages;

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
        cancel = view.findViewById(R.id.cancel);
        markAsUrgent = view.findViewById(R.id.markAsUrgent);
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
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fabButton.show();
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
        deleteFAB = view.findViewById(R.id.messageDeleteFAB);
        deleteFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteFAB.hide();
                fabButton.show();
                deleteMessages(deleteMessages);
                showRecycler();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markAsUrgent.setVisibility(View.INVISIBLE);
                cancel.setVisibility(View.INVISIBLE);
                fabButton.show();
                showRecycler();
            }
        });

        showRecyclerList();



        return view;
    }



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessageFragmentListener) context;
    }


    @Override
    public void sheetClicked(String string) {
        if (string == "new") {
            mListener.createNewMessages(this);
        } else if (string == "delete") {
            fabButton.hide();
            deleteFAB.show();
            deleteMessages = new ArrayList<>();
            adapter = new AllPrivateMessageAdapter(allMessageList, true,this);
            recyclerView.setAdapter(adapter);
        } else if (string == "notification") {
            mListener.createNotifications();
        }
        else if (string == "urgent") {
            markAsUrgent();
        }
    }
    public void markAsUrgent() {
        fabButton.hide();
        markAsUrgent.setVisibility(View.VISIBLE);
        cancel.setVisibility(View.VISIBLE);
        deleteMessages = new ArrayList<>();
        adapter = new AllPrivateMessageAdapter(allMessageList, true,this);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void goToPrivateChatFrag(String otherUserName, String otherUID, String messageKey) {
        mListener.goToPrivateChatFragment(otherUserName, otherUID, messageKey);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void createNewMessages(User otherUser) {
        // Update the user's messages id

        Map<String,Object> data = new HashMap<>();

        ArrayList<Map<String,Object>> members = new ArrayList<>();
        members.add(Map.of("uid",uid,"name",user.getDisplayName()));
        members.add(Map.of("uid",otherUser.getId(),"name",otherUser.getName()));
        data.put("members", members);
        data.put("time", Timestamp.now());
        data.put("recentMessage", "");

        String newDoc = messageRef.document().getId();
        messageRef.document(newDoc).set(data);

        db.collection("users").document(otherUser.getId()).update("messages", FieldValue.arrayUnion(newDoc));
        db.collection("users").document(uid).update("messages", FieldValue.arrayUnion(newDoc));


        mListener.goToPrivateChatFragment(otherUser.getName(),otherUser.getId(),newDoc);
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
    public void  deleteMessages(ArrayList<MessageList> list) {

        for (int i = 0; i < list.size(); i++){
            String mid = list.get(i).getKey();
            String otherUID = list.get(i).getMembers().getOtherMember(uid);
            Log.d(TAG, "deleteMessages: " + otherUID);
            db.collection("users").document(uid).update("messages", FieldValue.arrayRemove(mid));
            db.collection("users").document(otherUID).update("messages", FieldValue.arrayRemove(mid));
            deleteCollection(messageRef.document(mid).collection("messages"), Executors.newSingleThreadExecutor());
            messageRef.document(mid).delete();

        }


    }

    @Override
    public void deleteMessages(Boolean isChecked, MessageList messageList) {
        if (isChecked) {
            deleteMessages.add(messageList);
        }else {
            deleteMessages.remove(messageList);
        }
    }

    public interface MessageFragmentListener {
        void goToPrivateChatFragment(String otherUserName,String otherUserId, String messageKey);
        void createNewMessages(CreatePrivateMessages.iCreatePrivateMessages iListener);
        void createNotifications();
    }

    void showDialog() {
        // Create the fragment and show it as a dialog.

        BottomSheetDialogFragment bottom =  bottomSheetDialogFragment.newInstance(this);
        bottom.show(getParentFragmentManager(), "dialog");
    }


    // Initial the values for the recycler view / List of users
    public void showRecyclerList(){

            // Finds the user
           db.collection("users").document(uid)
                    .get().addOnSuccessListener( new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                         allMessageList = new ArrayList<>();
                          mid = (ArrayList<String>) documentSnapshot.getData().get("messages");

                          // Loops through each message id found
                          for (String id : mid) {

                              // Finds the document for the id
                              messageRef.document(id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                  @Override
                                  public void onEvent(@Nullable  DocumentSnapshot doc, @Nullable  FirebaseFirestoreException error) {
                                      Log.d(TAG, "onSuccess: " + id);
                                      // Checks to see if the document already exist in the arraylist
                                      for (int i = 0; i < allMessageList.size(); i++) {
                                          if (id == allMessageList.get(i).getKey()) {
                                              Log.d(TAG, "onEvent: " + id);
                                              allMessageList.remove(i);
                                          }
                                      }

                                      String message = (String) doc.get("recentMessage");
                                      Timestamp time = (Timestamp) doc.get("time");
                                      members = new Members();
                                      recentMessage = new Messages();
                                      recentMessage.setMessage(message);
                                      recentMessage.setTime(time);

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
                  });
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
                    if (time1.compareTo(time2) > 0 ) {
                        return -1;
                    } else if (time1.compareTo(time2) < 0 ){
                        return 1;
                    } else {
                        return time1.compareTo(time2);
                    }

                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });


        adapter = new AllPrivateMessageAdapter(allMessageList,this);
        recyclerView.setAdapter(adapter);
    }





}