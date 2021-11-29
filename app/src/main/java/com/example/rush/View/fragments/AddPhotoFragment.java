package com.example.rush.View.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.rush.Model.Member;
import com.example.rush.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class AddPhotoFragment extends Fragment {
    UploadFragmentListener mListener;
    private Button btnChoose, btnUpload, btnCancel;
    private ImageView imageView;
    //a Uri object to store file path
    private Uri filePath;
    FirebaseStorage storage;
    StorageReference storageReference;
    private final int PICK_IMAGE_REQUEST = 22;

    private FirebaseAuth mAuth;
    final String TAG = "UploadFragment";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reference = db.collection("users");
    private String messageKey;
    private boolean isPhotoAdded = false;
    private FirebaseUser user;

    public AddPhotoFragment( FirebaseUser user,String messageKey) {
        this.user = user;
        this.messageKey = messageKey;
        isPhotoAdded = true;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getActivity().setTitle("Add Photo");

        View view = inflater.inflate(R.layout.fragment_add_photo, container, false);

        btnChoose = (Button) view.findViewById(R.id.buttonChoose);
        btnUpload = (Button) view.findViewById(R.id.buttonUpload);
        btnCancel = (Button) view.findViewById(R.id.buttonCancel);

        imageView = (ImageView) view.findViewById(R.id.imageView);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        mAuth = FirebaseAuth.getInstance();

        btnChoose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();

            }
        });
        btnUpload.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if(imageView.getTag() != null) {
                    uploadImage();
                } else {
                    Toast.makeText(getActivity(), "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            /**
             * Clicking “Cancel” should replace this fragment with the Login Fragment.
             */
            @Override
            public void onClick(View v) {
                mListener.backFragment();
            }
        });


        return view;


    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (UploadFragmentListener) context;
    }

    public interface UploadFragmentListener{
        void backFragment();



    }

    private void SelectImage()
    {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data)
    {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getActivity().getContentResolver(),
                                filePath);
                Log.d("TAG", "onActivityResult: " + filePath);
                imageView.setImageBitmap(bitmap);
                imageView.setTag("Done");
            }

            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    // UploadImage method
    private void uploadImage() {
        if (filePath != null) {

            // Code for showing progressDialog while uploading
            ProgressDialog progressDialog
                    = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            "images/"
                                    + UUID.randomUUID().toString());

            // adding listeners on upload
            // or failure of image
            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.d(TAG, taskSnapshot.getMetadata().getReference().getPath());
                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    Toast
                                            .makeText(getActivity(),
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    storageReference.child(taskSnapshot.getMetadata().getPath()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // Got the download URL for 'users/me/profile.png'
                                            //Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();
                                            //generatedFilePath = downloadUri.toString(); /// The string(file link) that you need
                                            Log.d("testImageUrl", uri.toString());
                                            if (isPhotoAdded) {
                                                addImagesToMessage(uri.toString());
                                            }
                                            mListener.backFragment();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle any errors
                                        }
                                    });


                                    //Adding new Image

                                    Map<String, Object> photo = new HashMap<>();
                                    photo.put("imagePath", taskSnapshot.getMetadata().getPath());
                                    photo.put("postedDate", new Date());
                                    db.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("Photos").add(photo)
                                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                @Override
                                                public void onSuccess(DocumentReference documentReference) {
                                                    Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error adding document", e);
                                                }
                                            });



                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(getActivity(),
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            });
        }
    }

    public void addImagesToMessage(String img) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "");
        Timestamp time = Timestamp.now();
        data.put("time", time);
        data.put("uid", user.getUid());
        data.put("name", user.getDisplayName());
        data.put("isUrgent", false);
        data.put("img", img);

        db.collection("chat-messages")
                .document("private-messages")
                .collection("all-private-messages")
                .document(messageKey).collection("messages").add(data);
    }


}

