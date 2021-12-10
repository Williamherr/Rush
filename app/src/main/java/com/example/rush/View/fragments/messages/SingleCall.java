package com.example.rush.View.fragments.messages;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rush.Model.Member;
import com.example.rush.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;


public class SingleCall extends Fragment {


    private FirebaseUser user;
    private Member otherUser;
    private String mid;
    BottomNavigationView navBar;

    public SingleCall() {
        // Required empty public constructor
    }
    public SingleCall(FirebaseUser user,Member otherUser, String mid, BottomNavigationView navBar) {
        this.user = user;
        this.otherUser = otherUser;
        this.mid = mid;
        this.navBar = navBar;
        navBar.setVisibility(View.GONE);
    }

    private static final int PERMISSION_REQ_ID = 22;
    private static final String[] REQUESTED_PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
    };
    private boolean checkSelfPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(this.getContext(), permission) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this.getActivity(), REQUESTED_PERMISSIONS, requestCode);
            return false;
        }
        return true;
    }


    private String appId = "1ec0f7e102224f57be97e822104c0f58";
    // Fill the channel name.
    private String channelName = "";
    // Fill the temp token generated on Agora Console.
    private String token = "";
    private RtcEngine mRtcEngine;
    private View view;
    private ImageButton endCall;
    private ISingleCall iListener;

    final IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {
        @Override
        // Listen for the remote user joining the channel to get the uid of the user.
        public void onUserJoined(int uid, int elapsed) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("TAG", "run: " + uid);
                    // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                    SurfaceView surfaceView = RtcEngine.CreateRendererView(getContext());
                    FrameLayout container = view.findViewById(R.id.remote_video_view_container);
                    container.addView(surfaceView);
                    mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid));
                }
            });
        }
    };

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        iListener = (ISingleCall) context;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_single_call, container, false);
        endCall = view.findViewById(R.id.endCall);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
        }
        this.channelName = mid;
        this.token = fetchToken("https://rushs.herokuapp.com", channelName, 0);

        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
                checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel();
        }

        endCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iListener.endCall(otherUser,mid);
            }
        });





        return view;
    }

    public interface ISingleCall{
        void endCall(Member otherUser,String mid);
    }

    String fetchToken(String urlBase, String channelName, int userId) {
        OkHttpClient client = new OkHttpClient();
        String url = urlBase + "/access_token?channel=" + channelName + "&uid=" + userId;


        Request request = new Request.Builder()
                .url(url)
                .build();

        try {
            Response response = client.newCall(request).execute();
            Log.d("TAG", "fetchToken: " + response);
            JSONObject jsonObject = new JSONObject(response.body().string());
            return jsonObject.getString("token");
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }


        return "";
    }



    private void initializeAndJoinChannel() {
        try {
            mRtcEngine = RtcEngine.create(getContext(), appId, mRtcEventHandler);
        } catch (Exception e) {
            throw new RuntimeException("Check the error.");
        }

        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine.enableVideo();

        FrameLayout container = view.findViewById(R.id.local_video_view_container);
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        SurfaceView surfaceView = RtcEngine.CreateRendererView(getContext());
        container.addView(surfaceView);
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0));
        // Join the channel with a token.
        mRtcEngine.joinChannel(token, channelName, "", 0);
    }



    public void onDestroy() {
        super.onDestroy();
        navBar.setVisibility(View.VISIBLE);
        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
            mRtcEngine.destroy();
        }

    }
}