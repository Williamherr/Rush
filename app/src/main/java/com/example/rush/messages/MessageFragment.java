package com.example.rush.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.messages.model.PrivateMessageList;

import com.example.rush.messages.model.Messages;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;


import java.util.ArrayList;



public class MessageFragment extends Fragment {


    public MessageFragment() {
        // Required empty public constructor
    }
    ArrayList<PrivateMessageList> PrivateMessageList;

    LinearLayoutManager layoutManager;
    MessageFragmentListener mListener;
    MessageAdapter adapter;

    private String  uid = "LNQBoSfSxveCmlpa9jo1vdDzjrE3";
    private String otherUserName,otherPersonId, chatId = "";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");
    private final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    RecyclerView recyclerView;
    final String TAG = "MessageFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        recyclerView = view.findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        PrivateMessageList = new ArrayList<>();
        showRecyclerList();

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MessageFragmentListener) context;
    }

    public interface MessageFragmentListener {
        void goToPrivateChatFragment(String otherUserName,String otherUserId, String messageKey) ;
    }

    public void showRecyclerList(){
        db.runTransaction(new Transaction.Function<Void>() {

            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                Log.d(TAG,"transaction");
                DocumentSnapshot snapshot = transaction.get(reference.document(uid));
                ArrayList<String> allData = (ArrayList<String>) snapshot.getData().get("messages");

                for (String id: allData) {
                    CollectionReference ref = messageRef.document(id).collection("messages");
                    DocumentSnapshot messageSnapShot = transaction.get(messageRef.document(id));
                    ArrayList<String> idList = (ArrayList<String>) messageSnapShot.getData().get("uid");
                    chatId = id;


                    if (idList.contains(uid)) {
                        int index = idList.indexOf(uid);
                        if (index == 0) {
                            otherPersonId = idList.get(1);
                        } else {
                            otherPersonId = idList.get(0);
                        }
                    }

                    DocumentSnapshot nameSnap  =  transaction.get(messageRef.document(id).collection("members").document(otherPersonId));
                    otherUserName = (String) nameSnap.getData().get("name");


                    ref.limit(1).orderBy("time", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(  QuerySnapshot value, FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "listen:error", e);
                                        return;
                                    }
                                    Log.w(TAG, "listen:error");
                                    for (QueryDocumentSnapshot doc : value) {
                                        if (doc.get("message") != null) {
                                            String message = doc.getString("message");
                                            String name = doc.getString("name");
                                            Timestamp time = doc.getTimestamp("time");
                                            String uid = doc.getString("uid");
                                            PrivateMessageList.add(new PrivateMessageList(otherUserName,otherPersonId,new Messages(name,uid,message,time), chatId));
                                        }
                                    }
                                    adapter = new MessageAdapter(PrivateMessageList);
                                    recyclerView.setAdapter(adapter);
                                }
                            });

                }

                return null;
            }
        });
    }

    /*

        Adapter Class

     */

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewMessageHolder> {

        private ArrayList<PrivateMessageList> singleMessagesList = new ArrayList<>();

        public MessageAdapter(ArrayList<PrivateMessageList> singleMessagesList) {
            this.singleMessagesList = singleMessagesList;
        }


        public ViewMessageHolder onCreateViewHolder( ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.message_list,viewGroup,false);
            ViewMessageHolder holder = new ViewMessageHolder(view);

            return holder;
        }

        @Override
        public void onBindViewHolder( ViewMessageHolder holder, int position) {
            holder.name.setText(singleMessagesList.get(position).getOtherUserName());
            holder.message.setText(singleMessagesList.get(position).getMessage().getMessage());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String otherUserName = singleMessagesList.get(position).getOtherUserName();
                    String otherUserId = singleMessagesList.get(position).getOtheruserId();
                    String messageKey = singleMessagesList.get(position).getKey();
                    mListener.goToPrivateChatFragment(otherUserName,otherUserId,messageKey);
                }
            });
        }

        @Override
        public int getItemCount() {
            return singleMessagesList.size();
        }

        public class ViewMessageHolder extends RecyclerView.ViewHolder {

            private TextView name;
            private TextView message;

            public ViewMessageHolder(View view) {
                super(view);
                name = (TextView) view.findViewById(R.id.Name);
                message = (TextView) view.findViewById(R.id.message);
            }
    }
    }


}