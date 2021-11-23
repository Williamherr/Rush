package com.example.rush.View.fragments.classes;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rush.R;

public class ClassDetailsFragment extends Fragment {
    private String name, instructor, description;
    TextView text;


    public ClassDetailsFragment() {

    }

    public ClassDetailsFragment(String name, String instructor, String description) {
        this.name = name;
        this.instructor = instructor;
        this.description = description;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class_details, container, false);
        text = (TextView) view.findViewById(R.id.descriptionOfClassText);
        text.setText(description);
        return view;
    }
}