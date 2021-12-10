package com.example.rush.View.adapters.messages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.rush.Model.Messages;
import com.example.rush.R;
import com.example.rush.View.fragments.messages.bottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
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
    public void onBindViewHolder(MessageAdapter.ViewMessageHolder holder, @SuppressLint("RecyclerView") int position) {
        String uid = singleMessagesList.get(position).getUid();
        String img  = singleMessagesList.get(position).getImg();
        boolean isUrgent = singleMessagesList.get(position).getIsUrgent();
        holder.setIsRecyclable(false);

        // Other User
        if (!uid.equals(user.getUid())) {


            holder.receiveUserMessage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.showBottomMenu(singleMessagesList.get(position),position,true);
                    return false;
                }
            });
            holder.leftImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.showBottomMenu(singleMessagesList.get(position),position,true);
                    return false;
                }
            });



            holder.receiveUserMessage.setVisibility(View.VISIBLE);
            holder.userProfileImage.setVisibility(View.VISIBLE);
            holder.receiveUserMessage.setText(singleMessagesList.get(position).getMessage());
            holder.message.setVisibility(View.GONE);

            if (img != null && img != "") {
                holder.receiveUserMessage.setVisibility(View.GONE);
                holder.rightImage.setVisibility(View.GONE);
                holder.leftImage.setVisibility(View.VISIBLE);
                SelectImage(holder.leftImage,img);
            }
            if (isUrgent) {
                holder.receiveUserMessage.setBackground(view.getResources().getDrawable(R.drawable.error_message));
            }


        // Current User
        } else {

            holder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.showBottomMenu(singleMessagesList.get(position),position,false);
                    return false;
                }
            });
            holder.rightImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mListener.showBottomMenu(singleMessagesList.get(position),position,false);
                    return false;
                }
            });

            holder.receiveUserMessage.setVisibility(View.GONE);
            holder.userProfileImage.setVisibility(View.GONE);
            holder.message.setVisibility(View.VISIBLE);
            holder.message.setText(singleMessagesList.get(position).getMessage());
            if (img != null && img != "") {
                holder.message.setVisibility(View.GONE);
                holder.leftImage.setVisibility(View.GONE);
                holder.rightImage.setVisibility(View.VISIBLE);
                SelectImage(holder.rightImage, img);
            }
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
        private ImageView leftImage;
        private ImageView rightImage;

        public ViewMessageHolder(View view) {
            super(view);
            receiveUserMessage = (TextView) view.findViewById(R.id.receiveUserMessage);
            userProfileImage = (ImageView) view.findViewById(R.id.userProfileImage);
            message = (TextView) view.findViewById(R.id.message);
            messageMenuButton = view.findViewById(R.id.messageMenu);
            leftMessageMenuButton = view.findViewById(R.id.leftMessageMenu);
            leftImage = view.findViewById(R.id.leftImage);
            rightImage = view.findViewById(R.id.rightImage);


        }



    }



    private void SelectImage(ImageView imageView, String imgPath)
    {
        // Reference to an image file in Cloud Storage

        Glide.with(view.getContext())
                .load(imgPath)
                .into(imageView);


    }



    public interface IMessageAdapterListener {
        void showBottomMenu(Messages message, int position,boolean isOtherUser);
    }
}
