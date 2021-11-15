package com.example.rush.messages.Adapters;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rush.R;
import com.example.rush.messages.model.User;

import java.util.ArrayList;


public class SearchUserAdapter extends  RecyclerView.Adapter<SearchUserAdapter.ViewHolder> {

    ArrayList<User> users;
    final String TAG = "SearchUserAdapter";
    ISearchUserInterface iListner;

    public SearchUserAdapter() {
        // Required empty public constructor
    }
    public SearchUserAdapter(ArrayList<User> users,ISearchUserInterface iListner ) {
        this.users = users;
        this.iListner = iListner;
    }


    @Override
    public ViewHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_user_adapter, parent, false);
        SearchUserAdapter.ViewHolder holder = new SearchUserAdapter.ViewHolder(view);
        Log.d(TAG, "onCreateViewHolder: " + users.get(0).getName());
        return holder;
    }




    @Override
    public void onBindViewHolder(@NonNull  SearchUserAdapter.ViewHolder holder, int position) {
        holder.name.setText(users.get(position).getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iListner.createNewMessage(users.get(position));
            }
        });
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        public ViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.userName);

        }
    }

    public interface ISearchUserInterface {
        void createNewMessage(User user);
    }

}