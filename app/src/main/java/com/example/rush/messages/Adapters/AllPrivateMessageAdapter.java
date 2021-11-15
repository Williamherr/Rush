package com.example.rush.messages.Adapters;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.messages.model.Member;
import com.example.rush.messages.model.Members;
import com.example.rush.messages.model.MessageList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class AllPrivateMessageAdapter extends RecyclerView.Adapter<AllPrivateMessageAdapter.ViewMessageHolder> {
    boolean isDelete = false;
    private ArrayList<MessageList> messagesList = new ArrayList<>();
    private  IMessageFragmentInterface mListener;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    final String TAG = "AllPrivateMessageAdapter";
    String uid;

    public AllPrivateMessageAdapter(ArrayList<MessageList> singleMessagesList, IMessageFragmentInterface mListener) {
        this.mListener = mListener;
        this.messagesList = singleMessagesList;
        user = auth.getCurrentUser();
        uid = user.getUid();
    }
    public AllPrivateMessageAdapter(ArrayList<MessageList> singleMessagesList, boolean isDelete, IMessageFragmentInterface mListener) {
        this.mListener = mListener;
        this.isDelete = isDelete;
        this.messagesList = singleMessagesList;
        user = auth.getCurrentUser();
        uid = user.getUid();
    }


    public ViewMessageHolder onCreateViewHolder( ViewGroup viewGroup, int viewType) {
        View view;
        if (isDelete) {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.delete_private_messages,viewGroup,false);
        } else {
            view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.message_list,viewGroup,false);
        }

        ViewMessageHolder holder = new ViewMessageHolder(view);

        return holder;
    }

    @SuppressLint("LongLogTag")
    @Override
    public void onBindViewHolder( ViewMessageHolder holder, int position) {

        String otherUserName = "";
        String otherUserId = "";
        Log.d(TAG, "onBindViewHolder: " + uid);
        Members allMembers = messagesList.get(position).getMembers();
        for (int i = 0; i < allMembers.getAllMembers().size(); i++) {
            if (!(uid.equals(allMembers.getMember(i).getUid()))) {
                otherUserName = allMembers.getMember(i).getName();
                otherUserId = allMembers.getMember(i).getUid();
            } else {
                otherUserName = user.getDisplayName();
            }
        }

        String messageKey = messagesList.get(position).getKey();
        String recentMessage = messagesList.get(position).getMessages().getMessage();

        holder.name.setText(otherUserName);
        holder.message.setText(recentMessage);

        if (isDelete) {
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                  mListener.deleteMessages(b,messagesList.get(position));
                }
            });


        } else {
            String finalOtherUserName = otherUserName;
            String finalOtherUserId = otherUserId;
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.goToPrivateChatFrag(finalOtherUserName, finalOtherUserId,messageKey);

                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    public class ViewMessageHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private TextView message;
        private CheckBox checkBox;

        public ViewMessageHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.Name);
            message = (TextView) view.findViewById(R.id.message);
            if (isDelete) {
                checkBox = (CheckBox) view.findViewById(R.id.checkBox);

            }
        }
    }
    public interface IMessageFragmentInterface{
        void deleteMessages(Boolean isChecked, MessageList messsageKey);
        void goToPrivateChatFrag(String otherUserName, String otherUID, String messageKey);
    }
}


