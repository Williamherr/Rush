package com.example.rush.View.adapters.messages;

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

import com.example.rush.Model.Messages;
import com.example.rush.R;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewMessageHolder> {

    private ArrayList<Messages> singleMessagesList;
    String TAG = "MessageAdapter";
    IMessageAdapterListener mListener;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseUser user;
    View view;


    public MessageAdapter(ArrayList<Messages> singleMessagesList, FirebaseUser user, IMessageAdapterListener mListener) {
        this.user = user;
        this.singleMessagesList = singleMessagesList;
        this.mListener = mListener;

    }


    public void filterList(ArrayList<Messages> listFilter) {
        singleMessagesList = listFilter;

        //Let the adapter know the data set has changed
        notifyDataSetChanged();
    }


    public MessageAdapter.ViewMessageHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.user_messages_sent, viewGroup, false);

        MessageAdapter.ViewMessageHolder holder = new MessageAdapter.ViewMessageHolder(view);

        return holder;
    }


    @Override
    public void onBindViewHolder(MessageAdapter.ViewMessageHolder holder, int position) {
        String uid = singleMessagesList.get(position).getUid();
        String img  = singleMessagesList.get(position).getImg();
        boolean isUrgent = singleMessagesList.get(position).getIsUrgent();
        Log.d(TAG, "onBindViewHolder: ");
        Log.d(TAG, "isUrgent " + isUrgent);

        if (img == null || img.equals("")) {

        }

        holder.messageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, 0, singleMessagesList.get(position), position);
            }
        });
        holder.leftMessageMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, 1, singleMessagesList.get(position), position);
            }
        });


        if (!uid.equals(user.getUid())) {
            holder.receiveUserMessage.setVisibility(View.VISIBLE);
            holder.userProfileImage.setVisibility(View.VISIBLE);
            holder.message.setVisibility(View.INVISIBLE);
            holder.receiveUserMessage.setText(singleMessagesList.get(position).getMessage());

            if (isUrgent) {
                holder.receiveUserMessage.setBackground(view.getResources().getDrawable(R.drawable.error_message));
            }

        } else {
            holder.receiveUserMessage.setVisibility(View.INVISIBLE);
            holder.userProfileImage.setVisibility(View.INVISIBLE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(singleMessagesList.get(position).getMessage());
            if (isUrgent) {
                holder.message.setBackground(view.getResources().getDrawable(R.drawable.error_message));
            }
        }


    }

    @Override
    public int getItemCount() {
        return singleMessagesList.size();
    }

    public class ViewMessageHolder extends RecyclerView.ViewHolder {

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
                    trigger(messageMenuButton.getVisibility(), messageMenuButton);
                }
            });
            receiveUserMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    trigger(leftMessageMenuButton.getVisibility(), leftMessageMenuButton);
                }
            });


        }

        public void trigger(int inv, ImageButton menuButton) {
            switch (inv) {
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


    public void showPopup(View v, int id, Messages message, int position) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);

        if (id == 0) {
            popup.inflate(R.menu.message_menu);
        } else {
            popup.inflate(R.menu.left_message_menu);
        }


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Log.d(TAG, item.toString());

                switch (item.toString()) {
                    case "Edit":
                        mListener.update(message, position);
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
