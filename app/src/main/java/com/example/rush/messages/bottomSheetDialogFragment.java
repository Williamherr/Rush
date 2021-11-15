package com.example.rush.messages;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.rush.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class bottomSheetDialogFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private static IBottomSheetDialog iListener;
    private LinearLayout newMessages, deleteMessages, urgent;
    private LinearLayout notificationMessages;

    private String TAG = "BottomSheetDialogFragment";
    private int newMessageId, deleteMessageId;
    private int notificationMessageId;
    public bottomSheetDialogFragment() {
        // Required empty public constructor
    }

    public static bottomSheetDialogFragment newInstance(IBottomSheetDialog listener) {
        iListener = listener;
        Bundle args = new Bundle();
        bottomSheetDialogFragment fragment = new bottomSheetDialogFragment();
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
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_dialog, container, false);
        //New Messages
        newMessages  = view.findViewById(R.id.newClasses);
        newMessages.setOnClickListener(this);
        newMessageId = newMessages.getId();
        //Delete Messages
        deleteMessages  = view.findViewById(R.id.deleteClasses);
        deleteMessages.setOnClickListener(this);
        deleteMessageId = deleteMessages.getId();
        //add important Notifications
        notificationMessages  = view.findViewById(R.id.notificationClasses);
        notificationMessages.setOnClickListener(this);
        notificationMessageId = notificationMessages.getId();
        //Urgent messages
        urgent  = view.findViewById(R.id.urgent);
        urgent.setOnClickListener(this);


        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == newMessageId) {
            iListener.sheetClicked("new");
            getDialog().dismiss();
        } else if (view.getId() == deleteMessageId) {
            iListener.sheetClicked("delete");
            getDialog().dismiss();
        } else if (view.getId() == notificationMessageId) {
            iListener.sheetClicked("notification");
            getDialog().dismiss();
        }
        else if (view.getId() == R.id.urgent) {
            iListener.sheetClicked("urgent");
            getDialog().dismiss();
        }
    }

    interface IBottomSheetDialog{
        void sheetClicked(String string);
    }
}