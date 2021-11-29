package com.example.rush.View.fragments.account;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.example.rush.R;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.auth.User;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;


public class AccountFragment extends Fragment {

    FirebaseUser user;

    public AccountFragment( ) {
        // Required empty public constructor
    }

    public AccountFragment(FirebaseUser user) {
       this.user = user;

    }

    ImageView profilePic;
    TextView nameView, statusView;
    String status = "Online";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);
        profilePic = view.findViewById(R.id.profilePic);
        nameView = view.findViewById(R.id.name);
        statusView = view.findViewById(R.id.status);

        nameView.setText(user.getDisplayName());
        statusView.setText(status);






        return view;
    }



}

