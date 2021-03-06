package com.example.rush.View.fragments.messages;


import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;


import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;


import com.example.rush.R;
import com.example.rush.View.adapters.messages.MessageAdapter;
import com.example.rush.Model.Member;
import com.example.rush.Model.Messages;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class PrivateChatFragment extends Fragment implements MessageAdapter.IMessageAdapterListener, ReportDialogFragment.IreturnReport, PrivateChatBottomSheetMenu.IBottomSheetDialogMenu  {

    String messageKey = "";
    String TAG = "PrivateChatFragment";
    String userName;
    FirebaseUser user;
    String uid;
    String Report;
    Member otherUser;

    public PrivateChatFragment() {

    }


    public PrivateChatFragment(Member otherUser, String messageKey) {
        // Required empty public constructor
        this.otherUser = otherUser;
        this.messageKey = messageKey;
        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        userName = user.getDisplayName();

    }

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    MessageAdapter adapter;
    ArrayList<Messages> messages, searchList;
    EditText textview;
    ImageButton sendMessageButton, urgentButton, attachmentButton;
    private Messages message;
    //Attachment Photo for private message

    RecyclerView.SmoothScroller smoothScroller;

    final FirebaseFirestore db = FirebaseFirestore.getInstance();
    final CollectionReference messageRef = db.collection("chat-messages").document("private-messages").collection("all-private-messages");
    int position;
    boolean scrollToBottom = true;
    Messages reportMessage;
    private boolean isUrgent = false;
    boolean anyUrgentMessages = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_private_chat, container, false);
        position = 0;
        Log.d(TAG, "Private Chat Fragment");
        setHasOptionsMenu(true);

        messages = new ArrayList<>();
        searchList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        textview = view.findViewById(R.id.messageTextView);
        sendMessageButton = view.findViewById(R.id.messageSendButton);
        attachmentButton = view.findViewById(R.id.attachmentButton);
        urgentButton = view.findViewById(R.id.urgent);
        urgentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view);
            }
        });

        attachmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(getActivity(), "Please select an image", Toast.LENGTH_SHORT).show();
                mListener.addNewPhotoFragment(messageKey);
            }
        });


        addMessages();

        smoothScroller = new
                LinearSmoothScroller(getContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_START;
                    }
                };



        getChat();


        return view;
    }


    public void getChat() {
        messageRef.document(messageKey).collection("messages")
                .orderBy("time", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "listen:error", e);
                            return;
                        }
                        Map<String, Object> datas;
                        int i = 0;
                        anyUrgentMessages = false;
                        messages = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            if (doc.get("message") != null) {
                                String message = doc.getString("message");
                                String name = doc.getString("name");
                                Timestamp time = doc.getTimestamp("time");
                                String uid = doc.getString("uid");
                                Boolean urgent = doc.getBoolean("isUrgent");
                                String img = doc.getString("img");

                                if (urgent == null) {
                                    urgent = false;
                                }
                                if (urgent == true) {
                                    anyUrgentMessages = true;
                                }
                                if (img == null) {
                                    img = "";
                                } else {
                                    message = name + " sent a picture";
                                }



                                if (i == value.size() - 1) {
                                    datas = new HashMap<>();
                                    datas.put("recentMessage", message);
                                    datas.put("time", time);
                                    messageRef.document(messageKey).update(datas);
                                }
                                Log.d(TAG, "Doc ID:  " + doc.getId());
                                messages.add(new Messages(name, uid, doc.getId(), message, time, img, urgent));
                            }
                            i++;
                        }

                        if (anyUrgentMessages == true) {
                            messageRef.document(messageKey).update("isUrgent", true);
                        } else {
                            messageRef.document(messageKey).update("isUrgent", false);
                        }


                        showRecycler();
                    }
                });


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search_menu, menu);
        //Get the device's screen width


        DisplayMetrics metrics = new DisplayMetrics();
        (getActivity()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        try {
            SearchManager searchManager =
                    (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
            SearchView searchView =
                    (SearchView) menu.findItem(R.id.search_icon).getActionView();
            searchView.setSearchableInfo(
                    searchManager.getSearchableInfo(getActivity().getComponentName()));
            //Set the search bar maxWidth to around 75% of the device's screen width
            searchView.setMaxWidth((int)(width * 0.75));
            searchView.setQueryHint("Search messages");
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    filter(s);
                    return false;
                }
            });


        } catch (Exception e) {
            Toast.makeText(getActivity(), "Sorry, something went wrong!", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.video_icon:
                mListener.singleCallFragment(otherUser, messageKey);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    private void filter(String text) {
        ArrayList<Messages> filteredList = new ArrayList<>();

        for (Messages item : messages) {
            if (item.getMessage().toLowerCase(Locale.ROOT).contains(text.toLowerCase(Locale.ROOT))) {
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()) {
            adapter.filterList(messages);
            Toast.makeText(getActivity(), "No messages found", Toast.LENGTH_SHORT).show();
        } else {
            adapter.filterList(filteredList);
        }


    }

    public void showRecycler() {
        adapter = new MessageAdapter(messages, user,this);
        recyclerView.setAdapter(adapter);
        // Changes the position of the recycler view
        if (scrollToBottom) {
            //Scroll to the bottom if the page is onloaded
            layoutManager.scrollToPosition(adapter.getItemCount() - 1);
        } else {
            //Scroll to the specific position if the page is edited
            layoutManager.scrollToPosition(position);
            this.scrollToBottom = true;
        }


    }


    public void delete(Messages message) {
        messageRef.document(messageKey).collection("messages").document(message.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
    }


    public void update(Messages message) {
        this.scrollToBottom = false;

        textview.requestFocus();
        textview.setText(message.getMessage());
        showKeyboard();
        Log.d(TAG, "update: pos" + position);
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


    // Updates messages
    @RequiresApi(api = Build.VERSION_CODES.R)
    public void updateMessages(Messages message) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", textview.getText().toString());
        messageRef.document(messageKey).collection("messages").document(message.getId())
                .update(data);



        textview.setText("");
    }

    // Add messages to the Database
    public void addMessages() {
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "TAG");
                if (textview.getText().toString().trim().equals("")) {
                    return;
                }

                Map<String, Object> data = new HashMap<>();
                data.put("message", textview.getText().toString());
                Timestamp time = Timestamp.now();
                data.put("time", time);
                data.put("uid", uid);
                data.put("name", userName);
                data.put("isUrgent", isUrgent);



                textview.setText("");
                messageRef.document(messageKey).collection("messages").add(data);
                closeKeyboard();

            }
        });


    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (PrivateChatFragmentListener) context;
    }

    PrivateChatFragmentListener mListener;


    @Override
    public void sheetClicked(String item) {

        switch (item) {
            case "edit":
                update(message);
                break;
            case "report":
                Log.d(TAG, "onMenuItemClick: Report ");
                report(message);
                break;
            case "delete":
                Log.d(TAG, "onMenuItemClick: Delete");
                delete(message);
                break;
            case "resolve":
                Log.d(TAG, "onMenuItemClick: Resolve");
                resolve(message);
                break;


        }
    }




    public interface PrivateChatFragmentListener{
        void addNewPhotoFragment(String messageKey);
        void singleCallFragment(Member otherUser, String mid);
    }

    // This will make the urgent messages inside a chat false
    public void resolve(Messages message) {
        messageRef.document(messageKey).collection("messages")
                .document(message.getId())
                .update("isUrgent", false);
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void reportMessages(String report) {
        Messages message = this.reportMessage;

        Map<String, Object> data = new HashMap<>();
        ArrayList< Map<String, Object>> user = new ArrayList<>();
        ArrayList< Map<String, Object>> reportedUser = new ArrayList<>();

        user.add(Map.of("name", userName, "uid", uid));
        reportedUser.add(Map.of("name", message.getName(), "uid", message.getUid()));

        data.put("message", message.getMessage());
        data.put("reason", report);
        data.put("reportedUser", reportedUser);
        data.put("user", user);
        data.put("mid", message.getId());

        db.collection("chat-messages").document("private-messages").collection("reports")
                .add(data);
        Toast.makeText(getContext(), getContext().getString(R.string.MessageReportSuccess), Toast.LENGTH_SHORT).show();
    }

    void showDialog() {
        // Create the fragment and show it as a dialog.
        DialogFragment newFragment = ReportDialogFragment.newInstance(this);
        newFragment.show(getParentFragmentManager(), "dialog");
    }

    @Override
    public void showBottomMenu(Messages message, int position,boolean isOtherUser) {
        // Create the fragment and show it as a dialog.
        this.position = position;
        this.message = message;
        BottomSheetDialogFragment bottom = PrivateChatBottomSheetMenu.newInstance(this,isOtherUser);
        bottom.show(getParentFragmentManager(), "Bottom menu");
    }



    //send message and show ReportDialogFragment from message adapter

    public void report(Messages message) {
        this.Report = "";
        showDialog();
        this.reportMessage = message;

    }

    // Get report details from reportDialogFragment
    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void report(String report) {
        this.Report = report;
        reportMessages(report);
    }

    public void showKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void closeKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    // Shows Menu for priority
    public void showPopup(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), v);
        popup.inflate(R.menu.urgent_menu);



        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String itemName = item.toString();

                if (itemName.equals("Urgent")) {
                    isUrgent = true;
                } else {
                    isUrgent = false;
                }

                return false;
            }
        });


        popup.show();
    }

}






