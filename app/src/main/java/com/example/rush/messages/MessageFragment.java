package com.example.rush.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rush.R;

import com.example.rush.messages.Adapters.AllPrivateMessageAdapter;
import com.example.rush.messages.model.Members;
import com.example.rush.messages.model.Member;
import com.example.rush.messages.model.MessageList;
import com.example.rush.messages.model.Messages;
import com.example.rush.messages.model.PrivateMessageList;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.DateTime;


import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;


public class MessageFragment extends Fragment implements bottomSheetDialogFragment.IBottomSheetDialog, AllPrivateMessageAdapter.IMessageFragmentInterface {

    public MessageFragment() {
        // Required empty public constructor
    }

    FloatingActionButton fabButton;
    ExtendedFloatingActionButton deleteFAB;
    boolean isDelete = false;

    ArrayList<PrivateMessageList> PrivateMessageList;


    LinearLayoutManager layoutManager;
    MessageFragmentListener mListener;
    AllPrivateMessageAdapter adapter;
    private String uid;
    private String otherUserName,otherPersonId, chatId = "";
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

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
            mListener.createNewMessages();
        } else if (string == "delete") {
            fabButton.hide();
            deleteFAB.show();
            adapter = new AllPrivateMessageAdapter(allMessageList, true,this);
            recyclerView.setAdapter(adapter);
        } else if (string == "notification") {
            mListener.createNotifications();
        }
    }



    @Override
    public void deleteMessages(Boolean isChecked, String messsageKey) {

    }

    @Override
    public void goToPrivateChatFrag(String otherUserName, String otherUID, String messageKey) {
        mListener.goToPrivateChatFragment(otherUserName, otherUID, messageKey);
    }

    public interface MessageFragmentListener {
        void goToPrivateChatFragment(String otherUserName,String otherUserId, String messageKey);
        void createNewMessages();
        void createNotifications();
    }

    void showDialog() {
        // Create the fragment and show it as a dialog.

        BottomSheetDialogFragment bottom =  bottomSheetDialogFragment.newInstance(this);
        bottom.show(getParentFragmentManager(), "dialog");
    }
    void showRecycler() {
        adapter = new AllPrivateMessageAdapter(allMessageList,this);
        recyclerView.setAdapter(adapter);
    }

    public void showRecyclerList(){

        messageRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable  FirebaseFirestoreException error) {

            Task task1 = db.collection("users").document(uid).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    mid = (ArrayList<String>) documentSnapshot.getData().get("messages");
                }
            });

          Task task2 = Tasks.whenAllSuccess(task1);
          task2.addOnSuccessListener(new OnSuccessListener() {
              @Override
              public void onSuccess(Object o) {

                          allMessageList = new ArrayList<>();
                          for (String id : mid) {

                              Task messageTask = messageRef.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                  @Override
                                  public void onSuccess(DocumentSnapshot doc) {

                                      members = new Members();

                                      ArrayList<Map<String, Object>> users = (ArrayList<Map<String, Object>>) doc.get("members");

                                      String message = (String) doc.get("recentMessage");

                                      recentMessage = new Messages();
                                      recentMessage.setMessage(message);

                                      if (!users.equals(null)){
                                          for (Map<String,Object> member : users ) {
                                              Member mem = new Member(member.get("name").toString(),member.get("uid").toString());
                                              Log.d(TAG, "Member name: " + mem.getName());
                                              Log.d(TAG, "Member uid: " + mem.getUid());
                                              members.addMembers(mem);
                                          }

                                          messageList = new MessageList(members,recentMessage,id);
                                          allMessageList.add(messageList);
                                      }

                                  }
                              });


                              Task recyclerTask = Tasks.whenAllSuccess(messageTask);
                              recyclerTask.addOnSuccessListener(new OnSuccessListener<Object>() {
                                  @Override
                                  public void onSuccess(Object o) {
                                      showRecycler();

                                  }
                              });

                          }
                      }
                  });
                      }
                  });


//
//        Task t1 = db.runTransaction(new Transaction.Function<Void>() {
//
//            @Override
//            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
//                Log.d(TAG,"transaction");
//                DocumentSnapshot userDoc = transaction.get(reference.document(uid));
//                ArrayList<String> allMessageId = (ArrayList<String>) userDoc.getData().get("messages");
//
//                for (String id: allMessageId) {
//                    CollectionReference ref = messageRef.document(id).collection("messages");
//                    DocumentSnapshot messageSnapShot = transaction.get(messageRef.document(id));
//                    ArrayList<String> idList = (ArrayList<String>) messageSnapShot.getData().get("uid");
//                    chatId = id;
//
//
//                    if (idList.contains(uid)) {
//                        int index = idList.indexOf(uid);
//                        if (index == 0) {
//                            otherPersonId = idList.get(1);
//                        } else {
//                            otherPersonId = idList.get(0);
//                        }
//                    }
//
//                    DocumentSnapshot nameSnap  =  transaction.get(messageRef.document(id).collection("members").document(otherPersonId));
//                    otherUserName = (String) nameSnap.getData().get("name");
//
//
//                    ref.limit(1).orderBy("time", Query.Direction.DESCENDING)
//                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                                @Override
//                                public void onEvent(  QuerySnapshot value, FirebaseFirestoreException e) {
//                                    if (e != null) {
//                                        Log.w(TAG, "listen:error", e);
//                                        return;
//                                    }
//
//                                    Log.w(TAG, "listen:error");
//                                    for (QueryDocumentSnapshot doc : value) {
//                                        if (doc.get("message") != null) {
//                                            String message = doc.getString("message");
//                                            String name = doc.getString("name");
//                                            Timestamp time = doc.getTimestamp("time");
//                                            String uid = doc.getString("uid");
//
//                                            PrivateMessageList.add(new PrivateMessageList(otherUserName,otherPersonId,new Messages(name,uid, doc.getId(), message,time), chatId));
//
//                                        }
//                                    }
//                                    adapter = new MessageAdapter(PrivateMessageList);
//                                    recyclerView.setAdapter(adapter);
//                                }
//                            });
//
//                }
//
//                return null;
//            }
//        });

    }


    /*

        Adapter Class

     */



}