package com.example.rush.View.fragments.account;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.View.fragments.messages.MessageFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class AccountFragment extends Fragment implements View.OnClickListener,BottomSheetAccountSettings.IBottomSheetInterface {

    FirebaseUser user;
    String status;
    private FirebaseFirestore db;
    private IAccountSettingInterface iListener;

    public AccountFragment( ) {
        // Required empty public constructor
    }

    public AccountFragment(FirebaseUser user,FirebaseFirestore db) {
       this.user = user;
        this.db = db;
    }

    ImageView profilePic, statusColor;
    TextView nameView, statusView;
    private String TAG = "AccountFragment";
    private LinearLayout setStatus, accountInfo, signOut;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        iListener = (IAccountSettingInterface) context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);
        profilePic = view.findViewById(R.id.profilePic);
        statusColor = view.findViewById(R.id.statusColor);
        nameView = view.findViewById(R.id.name);
        statusView = view.findViewById(R.id.status);

        nameView.setText(user.getDisplayName());


        db.collection("users").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    status = (String) task.getResult().get("lastStatus");
                    getStatusColor();
                }
            }
        });


        setStatus = view.findViewById(R.id.setStatus);
        accountInfo = view.findViewById(R.id.accountInfo);
        signOut = view.findViewById(R.id.sign_out);

        setStatus.setOnClickListener(this);
        accountInfo.setOnClickListener(this);
        signOut.setOnClickListener(this);



        return view;
    }

    public void getStatusColor() {
        if (status == null) {
            status = "available";
        }
        statusView.setText(status);
        if (status.equals("available")) {
            statusColor.setColorFilter(getResources().getColor(R.color.status_available), PorterDuff.Mode.SRC_ATOP);
        } else if (status.equals("away")) {
            Log.d(TAG, "onCreateView: aw" );
            statusColor.setColorFilter(getResources().getColor(R.color.status_away), PorterDuff.Mode.SRC_ATOP);
        }else {
            Log.d(TAG, "onCreateView: e" );
            statusColor.setColorFilter(getResources().getColor(R.color.status_offline), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            Log.d(TAG, "bundle : ");

            Log.d(TAG, "bundle : " + savedInstanceState.getString("item"));
        }


    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.setStatus:
                Log.d(TAG, "setStatus ");
                updateStatus();
                break;
            case R.id.accountInfo:
                Log.d(TAG, "accountInfo ");
                break;
            case R.id.sign_out:
                Log.d(TAG, "signout ");
                iListener.signout();
                break;
            default:
                break;
        }
    }


    @Override
    public void sheetClicked(String item) {
        status = item;
        getStatusColor();
    }

    void updateStatus() {
        // Create the fragment and show it as a dialog.
        BottomSheetDialogFragment bottom = new BottomSheetAccountSettings(this, db,user.getUid());
        bottom.show(getParentFragmentManager(), "dialog");
    }

    public interface IAccountSettingInterface {
        void signout();
    }
}

