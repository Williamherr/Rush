package com.example.rush.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.rush.R;


public class ReportDialogFragment extends DialogFragment {

    private static IreturnReport reportListener;
    EditText reason;
    Button cancel, send;


    static ReportDialogFragment newInstance(IreturnReport reportInterface) {
        reportListener = reportInterface;
        return new ReportDialogFragment();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.report, container, false);
        reason = v.findViewById(R.id.reason);
        cancel = v.findViewById(R.id.cancelButton);
        send = v.findViewById(R.id.sendButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportListener.report(reason.getText().toString());
                getDialog().dismiss();
            }
        });

        return v;
    }

    interface IreturnReport{
        void report(String reportListener);
    }

}