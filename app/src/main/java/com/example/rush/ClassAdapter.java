package com.example.rush;

import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ViewHolder> {
    private ArrayList<ClassInfo> classList;

    public ClassAdapter(ArrayList<ClassInfo> classList) {
        this.classList = classList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_classes,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassInfo classObj = classList.get(position);
        String totalText = "";
        totalText += "Class Name: \n" + classObj.getClassName() + "\n" +
                "Instructor: \n" + classObj.getInstructor()+ "\n" +
                "Description: \n" + classObj.getDescription();
        holder.info.setText(totalText);
       // holder.description.setText(classObj.getDescription());
    }

    @Override
    public int getItemCount() {
        if (classList != null) {
            return classList.size();
        } else {
            return 0;
        }
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView info;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            info = view.findViewById(R.id.classesText);
        }

    }
}
