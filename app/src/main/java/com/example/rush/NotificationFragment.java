package com.example.rush;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class NotificationFragment extends Fragment {
    NotificationFragmentListener mListener;
    private EditText inputNotificationMessage;
    private Button btnPushNotification,btnAddPhoto;
    private FirebaseAuth mAuth;
    final String TAG = "NotificationFragment";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        inputNotificationMessage = view.findViewById(R.id.notification_message);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();


        btnPushNotification = (Button) view.findViewById(R.id.notification);
        btnAddPhoto = (Button) view.findViewById(R.id.buttonAddPhoto);

        btnPushNotification.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (validate()) {
                    String notificationMessage = inputNotificationMessage.getText().toString();
                    mAuth = FirebaseAuth.getInstance();
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (  user.getDisplayName() == null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName("Fake Name").build();
                        user.updateProfile(profileUpdates);
                    }
                    Log.d(TAG,user.getUid());
                    addNotification(notificationMessage, user.getEmail().toString());
                }
            }
        });

        btnAddPhoto.setOnClickListener(v -> {
            mListener.addNewPhotoFragment();
        });
        return view;


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (NotificationFragmentListener) context;
    }

    public interface NotificationFragmentListener{
        void addNewPhotoFragment();

    }

    public Boolean validate(){
        String notificationMessage = inputNotificationMessage.getText().toString();
        if(notificationMessage.replaceAll("\\s", "").isEmpty()){
            Toast.makeText(getActivity(), "Notification Message cannot be empty", Toast.LENGTH_SHORT);
            return false;
        }

        return true;
    }

    private void addNotification(String notificationMessage, String userEmail) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("Notifications from email " + userEmail)
                        .setContentText(notificationMessage);//set Content of Notification

        Intent notificationIntent = new Intent(getContext(),MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Add as notification
        NotificationManager manager = ( NotificationManager ) getActivity().getSystemService( getActivity().NOTIFICATION_SERVICE );
        manager.notify(0, builder.build());
    }



}

