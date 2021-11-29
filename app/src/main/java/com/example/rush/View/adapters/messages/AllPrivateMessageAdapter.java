package com.example.rush.View.adapters.messages;

import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.Model.Member;
import com.example.rush.Model.MessageList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;


public class AllPrivateMessageAdapter extends RecyclerView.Adapter<AllPrivateMessageAdapter.ViewMessageHolder> {
    boolean isDelete = false;
    private ArrayList<MessageList> messagesList;
    private  IMessageFragmentInterface mListener;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser user;
    final String TAG = "AllPrivateMessageAdapter";
    String uid;
    View view;

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


    @Override
    public void onBindViewHolder(ViewMessageHolder holder, @SuppressLint("RecyclerView") int position) {

        Member otherUser =  messagesList.get(position).getMembers().getOtherMember(uid);
        String otherUserName = otherUser.getName();
        String otherUserId = otherUser.getUid();


        boolean isUrgent = messagesList.get(position).getMessages().getIsUrgent();

        if (isUrgent) {
            holder.itemView.setBackgroundColor(view.getResources().getColor(R.color.error_message));
        }

        String messageKey = messagesList.get(position).getKey();
        String recentMessage = messagesList.get(position).getMessages().getMessage();

        holder.name.setText(otherUserName);
        holder.message.setText(recentMessage);

        if (isDelete) {
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                  mListener.editMessages(b,messagesList.get(position));
                }
            });


        } else {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mListener.goToPrivateChatFrag(otherUser,messageKey);

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
        void editMessages(Boolean isChecked, MessageList messsageKey);
        void goToPrivateChatFrag(Member otherUser, String messageKey);
    }
}


