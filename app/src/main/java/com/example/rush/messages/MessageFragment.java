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

import com.google.android.gms.tasks.OnCompleteListener;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;


import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MessageFragment extends Fragment {


    public MessageFragment() {
        // Required empty public constructor
    }
    ArrayList<PrivateMessageList> PrivateMessageList = new ArrayList<>();

    LinearLayoutManager layoutManager;
    MessageFragmentListener mListener;
    MessageAdapter adapter;

    private String  uid = "LNQBoSfSxveCmlpa9jo1vdDzjrE3";
    private String otherUserName,otherPersonId, chatId = "";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");
    private final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    RecyclerView recyclerView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        recyclerView = view.findViewById(R.id.RecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        showRecyclerList();

        return view;
    }

    @Override
    public void onAttach(Context context) {
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
                Log.d("TAG","transaction");
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

                    Task task = ref.limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {

                            if (task.isSuccessful()) {

                                for (QueryDocumentSnapshot document : task.getResult()) {

                                    String message = document.get("message").toString();
                                    com.google.firebase.Timestamp time = (Timestamp) document.get("time");
                                    String messageUid = document.get("uid").toString();
                                    String currentName = document.get("name").toString();
                                    PrivateMessageList.add(new PrivateMessageList(otherUserName,otherPersonId,new Messages(currentName,messageUid,message,time), chatId));
                                }
                                adapter = new MessageAdapter(PrivateMessageList);
                                recyclerView.setAdapter(adapter);

                            }
                        }
                    });
                    try {
                        // Block on a task and get the result synchronously. This is generally done
                        // when executing a task inside a separately managed background thread. Doing this
                        // on the main (UI) thread can cause your application to become unresponsive.
                        Tasks.await(task);
                    }  catch (InterruptedException e) {
                        // An interrupt occurred while waiting for the task to complete.
                        // ...
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
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