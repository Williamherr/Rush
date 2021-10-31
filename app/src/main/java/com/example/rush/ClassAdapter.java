package com.example.rush;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.class_items,
                parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ClassInfo classObj = classList.get(position);
        SpannableString[] stringSpanners = new SpannableString[]{
                new SpannableString("Class Name"), new SpannableString("Instructor"),
                new SpannableString("Description")
        };

        /*
                Use SpannableStrings to set only the first word in the TextViews bold
         */
        stringSpanners[0].setSpan(new StyleSpan(Typeface.BOLD), 0,
                stringSpanners[0].length(), 0);
        stringSpanners[0].setSpan(new UnderlineSpan(), 0, stringSpanners[0].length(), 0);
        stringSpanners[0].setSpan(new RelativeSizeSpan(1.2f), 0, stringSpanners[0].length(), 0);
        stringSpanners[1].setSpan(new StyleSpan(Typeface.BOLD), 0,
                stringSpanners[1].length(), 0);
        stringSpanners[1].setSpan(new UnderlineSpan(), 0, stringSpanners[1].length(), 0);
        stringSpanners[1].setSpan(new RelativeSizeSpan(1.2f), 0, stringSpanners[1].length(), 0);
        stringSpanners[2].setSpan(new StyleSpan(Typeface.BOLD), 0,
                stringSpanners[2].length(), 0);
        stringSpanners[2].setSpan(new UnderlineSpan(), 0, stringSpanners[2].length(), 0);
        stringSpanners[2].setSpan(new RelativeSizeSpan(1.2f), 0, stringSpanners[2].length(), 0);

        holder.className.setText(stringSpanners[0]);
        holder.className.append("\n" + classObj.getClassName());
        holder.instructorName.setText(stringSpanners[1]);
        holder.instructorName.append("\n" + classObj.getInstructor());
        holder.classDescription.setText(stringSpanners[2]);
        holder.classDescription.append("\n" + classObj.getDescription());
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
        public TextView className;
        public TextView instructorName;
        public TextView classDescription;

        public ViewHolder(View view) {
            super(view);
            this.view = view;
            className = view.findViewById(R.id.classNameText);
            instructorName = view.findViewById(R.id.classInstructorName);
            classDescription = view.findViewById(R.id.classDescriptionText);
        }

    }
}
