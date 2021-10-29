package com.example.rush.messages;

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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.messages.model.Messages;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.core.OrderBy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;


public class PrivateChatFragment extends Fragment {


    String otherUserName, otherUserId, messageKey = "";
    String TAG = "PrivateChatFragment";
    String userName, getOtherUserId = "";

    String uid = "LNQBoSfSxveCmlpa9jo1vdDzjrE3";

    public PrivateChatFragment(String otherUserName, String otherUserId, String messageKey) {
        // Required empty public constructor
        this.otherUserId = otherUserId;
        this.otherUserName = otherUserName;
        this.messageKey = messageKey;

    }

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    MessageAdapter adapter;
    ArrayList<Messages> messages;
    EditText textview;
    ImageButton sendMessageButton;
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    String s = "String";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_private_chat, container, false);
        Log.d(TAG,"Private Chat Fragment");
        userName = "William Herr";
        messages = new ArrayList<>();
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        textview = view.findViewById(R.id.messageTextView);
        sendMessageButton = view.findViewById(R.id.messageSendButton);



        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"TAG");
                if (textview.getText().toString().trim().equals("") ){
                    return;
                }
                Map<String,Object> data = new HashMap<>();
                data.put("message",  textview.getText().toString());
                Timestamp time = Timestamp.now();
                data.put("time", time);
                data.put("uid",uid);
                data.put("name",userName);


                textview.setText("");
                Task task = messageRef.document(messageKey).collection("messages")
                        .add(data);


                // Update the user's messages id
             /*   task.continueWithTask(new Continuation() {
                    @Override
                    public Task then(@NonNull  Task task) throws Exception {

                        Task t = db.collection("usersss").document("LNQBoSfSxveCmlpa9jo1vdDzjrE3")

                                .update("messages",FieldValue.arrayUnion(st.get(0)))

                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "DocumentSnapshot written with ID: " + unused);
                                        Log.d(TAG, data.toString());
                                    }
                                });

                        return t;
                    }


              });*/



            }
        });









        messageRef.document(messageKey).collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable  QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "listen:error", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("message") != null) {
                        String message = doc.getString("message");
                        String name = doc.getString("name");
                        Timestamp time = doc.getTimestamp("time");
                        String uid = doc.getString("uid");

                        messages.add(new Messages(name, uid, message, time));

                    }
                }
                adapter = new MessageAdapter(messages,userName);
                recyclerView.setAdapter(adapter);
            }
        });



        return  view;
    }


    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewMessageHolder> {

        private ArrayList<Messages> singleMessagesList = new ArrayList<>();
        private String userName;

        public MessageAdapter(ArrayList<Messages> singleMessagesList, String userName) {
            this.singleMessagesList = singleMessagesList;
            this.userName = userName;
        }


        public MessageAdapter.ViewMessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.user_messages_sent,viewGroup,false);

            MessageAdapter.ViewMessageHolder holder = new MessageAdapter.ViewMessageHolder(view);

            return holder;
        }


        @Override
        public void onBindViewHolder(MessageAdapter.ViewMessageHolder holder, int position) {
            String user = singleMessagesList.get(position).getName();

            if (!(user.equals(userName))) {
                Log.d(TAG, "otherUserId");
                holder.receiveUserProfileImage.setVisibility(View.VISIBLE);
                holder.receiveUserMessage.setVisibility(View.VISIBLE);
                holder.userProfileImage.setVisibility(View.INVISIBLE);
                holder.message.setVisibility(View.INVISIBLE);
                holder.receiveUserMessage.setText(singleMessagesList.get(position).getMessage());

            } else {
                Log.d(TAG,"user");
                holder.receiveUserProfileImage.setVisibility(View.INVISIBLE);
                holder.receiveUserMessage.setVisibility(View.INVISIBLE);
                holder.userProfileImage.setVisibility(View.VISIBLE);
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(singleMessagesList.get(position).getMessage());
            }



            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                }
            });
        }

        @Override
        public int getItemCount() {
            return singleMessagesList.size();
        }

        public class ViewMessageHolder extends RecyclerView.ViewHolder {

            private ImageView userProfileImage;
            private ImageView receiveUserProfileImage;
            private TextView receiveUserMessage;
            private TextView message;

            public ViewMessageHolder(View view) {
                super(view);
                receiveUserProfileImage = (ImageView) view.findViewById(R.id.receiveUserProfileImage);
                receiveUserMessage = (TextView) view.findViewById(R.id.receiveUserMessage);

                userProfileImage = (ImageView) view.findViewById(R.id.userProfileImage);
                message = (TextView) view.findViewById(R.id.message);

            }
        }
    }


}
