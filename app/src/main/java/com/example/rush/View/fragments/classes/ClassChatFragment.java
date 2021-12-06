package com.example.rush.View.fragments.classes;

import android.app.SearchManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.rush.MainActivity;
import com.example.rush.Model.Messages;
import com.example.rush.R;
import com.example.rush.View.adapters.messages.MessageAdapter;
import com.example.rush.View.fragments.messages.PrivateChatBottomSheetMenu;
import com.example.rush.View.fragments.messages.PrivateChatFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ClassChatFragment extends Fragment {
    private String id, userName, className;
    private String userID;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private EditText messageText;
    private ImageButton sendMessageButton, attachmentButton;
    private CollectionReference messageRef;
    private ArrayList<Messages> messages;
    private RecyclerView recycle;
    private ClassChatAdapter adapter;


    public ClassChatFragment() {

    }

    public ClassChatFragment(String id, String className) {
        this.id = id;
        this.className = className;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        messageRef = database.collection("chat-messages").document("class-messages").collection(id);
        if (user != null) {
            userID = user.getUid();
            userName = user.getDisplayName();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_class_chat, container, false);

        messages = new ArrayList<>();

        messageText = view.findViewById(R.id.messageTextView);
        sendMessageButton = view.findViewById(R.id.messageSendButton);
        attachmentButton = view.findViewById(R.id.attachmentButton);

        recycle = (RecyclerView) view.findViewById(R.id.classMessagesRecycler);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(getActivity());
        recycle.setLayoutManager(manager);

        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back);
        actionBar.setDisplayHomeAsUpEnabled(true);

        addMessages();
        getChat();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        ((MainActivity) getActivity()).backFragment();

        return true;
    }

    private void addMessages() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (messageText.getText().toString().trim().equals("")) {
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("message", messageText.getText().toString());
                Timestamp time = Timestamp.now();
                data.put("time", time);
                data.put("uid", userID);
                data.put("name", userName);

                messageText.setText("");
                String docID = messageRef.document().getId();
                messageRef.document(docID).set(data);


            }
        });
    }

    public void update(Messages message) {

        messageText.requestFocus();
        messageText.setText(message.getMessage());
        showKeyboard();
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                updateMessages(message);
                closeKeyboard();
                addMessages();
            }
        });
    }

    public void delete(Messages message) {
        messageRef.document(message.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public void showBottomMenu(Messages message) {
        BottomSheetDialog bottom = new BottomSheetDialog(getActivity());
        bottom.setContentView(R.layout.fragment_classes_bottom_dialog);
        LinearLayout edit = bottom.findViewById(R.id.newClasses);
        LinearLayout delete = bottom.findViewById(R.id.deleteClasses);
        //Change the text to show the edit and delete options
        TextView editMessage = ((TextView) edit.findViewById(R.id.dialogNew));
        editMessage.setText("Edit");
        TextView deleteMessage = ((TextView) delete.findViewById(R.id.dialogDelete));
        deleteMessage.setText("Delete");
        bottom.show();
        //ALlow the user to edit messages
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update(message);
                bottom.dismiss();
            }
        });
        //Allow the user to delete messages
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                delete(message);
                bottom.dismiss();
            }
        });

    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void updateMessages(Messages message) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", messageText.getText().toString());
        messageRef.document(message.getId()).update(data);

        messageText.setText("");
    }


    private void getChat() {
        messageRef.orderBy("time", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                messages = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    if (doc.get("message") != null) {
                        String message = doc.getString("message");
                        String name = doc.getString("name");
                        Timestamp time = doc.getTimestamp("time");
                        String uid = doc.getString("uid");
                        String img = doc.getString("img");
                        if (img == null) {
                            img = "";
                        } else {
                            message = name + " sent a picture";
                        }

                        messages.add(new Messages(name, uid, doc.getId(), message, time, img));
                        Log.d("ClassChat", message);

                    }
                }
                adapter = new ClassChatAdapter(messages);
                recycle.setAdapter(adapter);
            }
        });
    }

    public class ClassChatAdapter extends RecyclerView.Adapter<ClassChatFragment.ClassChatAdapter.ViewHolder> {
        private ArrayList<Messages> messagesList;

        private void SelectImage(ImageView imageView, String imgPath) {
            Glide.with(getContext())
                    .load(imgPath)
                    .into(imageView);
        }


        public ClassChatAdapter(ArrayList<Messages> messagesList) {
            this.messagesList = messagesList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.user_messages_sent,
                    parent, false);
            return new ClassChatFragment.ClassChatAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String uid = messagesList.get(position).getUid();
            String name = messagesList.get(position).getName();
            String img = messagesList.get(position).getImg();
            SpannableString stringSpanner = new SpannableString(name);
            stringSpanner.setSpan(new StyleSpan(Typeface.BOLD), 0, stringSpanner.length(), 0);


            attachmentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(getActivity(), "Please select an image", Toast.LENGTH_SHORT).show();
                    ((MainActivity) getActivity()).addNewPhotoFragment(id, messagesList.get(position).getId());

                }
            });


            if (!uid.equals(user.getUid())) {
                holder.receiveUserMessage.setVisibility(View.VISIBLE);
                holder.userProfileImage.setVisibility(View.VISIBLE);
                holder.receiveUserMessage.setText(messagesList.get(position).getMessage());
                holder.message.setVisibility(View.GONE);
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(stringSpanner);


                if (img != null && img != "") {
                    holder.receiveUserMessage.setVisibility(View.GONE);
                    holder.rightImage.setVisibility(View.GONE);
                    holder.leftImage.setVisibility(View.VISIBLE);
                    SelectImage(holder.leftImage, img);
                }
            } else {
                holder.receiveUserMessage.setVisibility(View.GONE);
                holder.userProfileImage.setVisibility(View.GONE);
                holder.message.setVisibility(View.VISIBLE);
                holder.message.setText(messagesList.get(position).getMessage());
                holder.name.setVisibility(View.GONE);
                holder.rightImage.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showBottomMenu(messagesList.get(position));
                        return false;
                    }
                });
                holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        showBottomMenu(messagesList.get(position));
                        return false;
                    }
                });
                if (img != null && img != "") {
                    holder.message.setVisibility(View.GONE);
                    holder.leftImage.setVisibility(View.GONE);
                    holder.rightImage.setVisibility(View.VISIBLE);
                    SelectImage(holder.rightImage, img);
                }
            }


        }

        @Override
        public int getItemCount() {
            if (messagesList != null) {
                return messagesList.size();
            } else {
                return 0;
            }
        }

        public void filterList(ArrayList<Messages> messages) {
            this.messagesList = messages;
            notifyDataSetChanged();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private View view;
            private ImageView userProfileImage;
            private TextView receiveUserMessage;
            private TextView message;
            private TextView name;
            private ImageButton messageMenuButton;
            private ImageButton leftMessageMenuButton;
            private ImageView leftImage;
            private ImageView rightImage;

            public ViewHolder(View view) {
                super(view);
                this.view = view;
                receiveUserMessage = (TextView) view.findViewById(R.id.receiveUserMessage);
                userProfileImage = (ImageView) view.findViewById(R.id.userProfileImage);
                message = (TextView) view.findViewById(R.id.message);
                messageMenuButton = view.findViewById(R.id.messageMenu);
                leftMessageMenuButton = view.findViewById(R.id.leftMessageMenu);
                name = view.findViewById(R.id.displayName);
                leftImage = view.findViewById(R.id.leftImage);
                rightImage = view.findViewById(R.id.rightImage);

            }
        }

    }

}