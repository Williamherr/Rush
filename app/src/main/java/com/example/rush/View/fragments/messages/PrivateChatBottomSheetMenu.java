package com.example.rush.View.fragments.messages;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rush.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class PrivateChatBottomSheetMenu extends BottomSheetDialogFragment implements View.OnClickListener {

    private IBottomSheetDialogMenu iListener;
    private LinearLayout edit, delete, report, resolve;


    private String TAG = "BottomSheetDialogFragment";
    private boolean isOtherUser = false;

    public PrivateChatBottomSheetMenu(IBottomSheetDialogMenu listener, Boolean isOtherUser) {
        // Required empty public constructor
        this.isOtherUser = isOtherUser;
        this.iListener = listener;
    }

    public static PrivateChatBottomSheetMenu newInstance( IBottomSheetDialogMenu listener, Boolean isOtherUser) {
        Bundle args = new Bundle();
        PrivateChatBottomSheetMenu fragment = new PrivateChatBottomSheetMenu(listener,isOtherUser);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.bottom_sheet_private_chat_menu, container, false);
        //New Messages
        Log.d(TAG, "onCreateView: PrivateChat");




        edit = view.findViewById(R.id.edit);
        delete = view.findViewById(R.id.delete);
        report = view.findViewById(R.id.report);
        resolve = view.findViewById(R.id.resolve);

        edit.setOnClickListener(this);
        delete.setOnClickListener(this);
        report.setOnClickListener(this);
        resolve.setOnClickListener(this);

        if (isOtherUser) {
            delete.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);

        } else {
            report.setVisibility(View.GONE);
        }


        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.edit) {
            iListener.sheetClicked("edit");
            getDialog().dismiss();
        } else if (view.getId() == R.id.delete) {
            iListener.sheetClicked("delete");
            getDialog().dismiss();
        } else if (view.getId() == R.id.report) {
            iListener.sheetClicked("report");
            getDialog().dismiss();
        }
        else if (view.getId() == R.id.resolve) {
            iListener.sheetClicked("resolve");
            getDialog().dismiss();
        }

    }


    interface IBottomSheetDialogMenu{
        void sheetClicked(String string);
    }




}

