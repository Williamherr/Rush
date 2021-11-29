package com.example.rush.View.fragments.account;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rush.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.FirebaseFirestore;


public class BottomSheetAccountSettings extends BottomSheetDialogFragment implements View.OnClickListener {

    private IBottomSheetInterface iListener;
    private LinearLayout available, away, offline;
    private FirebaseFirestore db;
    private String uid;

    private String TAG = "BottomSheetDialogFragment";


    public BottomSheetAccountSettings() {
        // Required empty public constructor
    }

    public BottomSheetAccountSettings(IBottomSheetInterface iListener,FirebaseFirestore db,String uid) {
        this.iListener = iListener;
        this.db = db;
        this.uid = uid;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_account_settings, container, false);
        //New Messages
        available = view.findViewById(R.id.available);
        away = view.findViewById(R.id.away);
        offline = view.findViewById(R.id.offline);

        available.setOnClickListener(this);
        away.setOnClickListener(this);
        offline.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        String item = "";
        switch (id) {
            case R.id.available:
                item = "available";
                updateStatus(item);
                break;
            case R.id.away:
                item = "away";
                updateStatus(item);
                break;
            case R.id.offline:
                item = "offline";
                updateStatus(item);
                break;
            default:
                break;
        }
        iListener.sheetClicked(item);
        getDialog().dismiss();
    }

    public void updateStatus(String item) {
        db.collection("users").document(uid).update("lastStatus",item);
        db.collection("users").document(uid).update("status",item);
    }

    public interface IBottomSheetInterface{
        void sheetClicked(String string);
    }
}