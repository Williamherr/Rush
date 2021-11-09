package com.example.rush.messages.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.rush.R;
import com.example.rush.messages.model.Messages;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewMessageHolder> {

    private ArrayList<Messages> singleMessagesList = new ArrayList<>();
    private String userName;
    String TAG = "PrivateChatAdapter";
    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    IMessageAdapterListener mListener;

    public MessageAdapter(ArrayList<Messages> singleMessagesList, String userName,IMessageAdapterListener mListener ) {
        this.singleMessagesList = singleMessagesList;
        this.userName = userName;
        this.mListener = mListener;
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
        String id = singleMessagesList.get(position).getId();
        holder.messageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v,0,singleMessagesList.get(position),position);
            }
        });
        holder.leftMessageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v,1,singleMessagesList.get(position),position);
            }
        });
        if (!(user.equals(userName))) {
            holder.receiveUserMessage.setVisibility(View.VISIBLE);
            holder.userProfileImage.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.INVISIBLE);
            holder.receiveUserMessage.setText(singleMessagesList.get(position).getMessage());


        } else {
            holder.receiveUserMessage.setVisibility(View.INVISIBLE);
            holder.userProfileImage.setVisibility(View.INVISIBLE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(singleMessagesList.get(position).getMessage());

        }




    }

    @Override
    public int getItemCount() {
        return singleMessagesList.size();
    }

    public class ViewMessageHolder extends RecyclerView.ViewHolder  {

        private ImageView userProfileImage;
        private TextView receiveUserMessage;
        private TextView message;
        private ImageButton messageMenuButton;
        private ImageButton leftMessageMenuButton;


        public ViewMessageHolder(View view) {
            super(view);
            receiveUserMessage = (TextView) view.findViewById(R.id.receiveUserMessage);

            userProfileImage = (ImageView) view.findViewById(R.id.userProfileImage);
            message = (TextView) view.findViewById(R.id.message);
            messageMenuButton = view.findViewById(R.id.messageMenu);
            leftMessageMenuButton = view.findViewById(R.id.leftMessageMenu);

            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trigger(messageMenuButton.getVisibility(),messageMenuButton);
                }
            });
            receiveUserMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trigger(leftMessageMenuButton.getVisibility(),leftMessageMenuButton);
                }
            });


        }

        public void trigger(int inv,ImageButton menuButton) {
            switch(inv){
                case 0:
                    menuButton.setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    menuButton.setVisibility(View.VISIBLE);
                    break;
                default:
                    Log.d(TAG, "" + inv);
                    break;
            }
        }


    }



    public void showPopup(View v, int id,Messages message, int position) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);

        if (id ==  0) {
            popup.inflate(R.menu.message_menu);
        }
        else {
            popup.inflate(R.menu.left_message_menu);
        }


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG,item.toString());

                switch ( item.toString()) {
                    case "Edit":
                        mListener.update(message,position);
                        break;
                    case "Report":
                        Log.d(TAG, "onMenuItemClick: Report ");
                        mListener.report(message);
                        break;
                    case "Delete":
                        Log.d(TAG, "onMenuItemClick: Delete");
                        mListener.delete(message);
                        break;

                }

                return false;
            }
        });


        popup.show();
    }

    public interface IMessageAdapterListener {
        void update(Messages message, int position);
        void delete(Messages message);
        void report(Messages message);
    }
}
