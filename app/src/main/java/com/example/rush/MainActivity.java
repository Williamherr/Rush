package com.example.rush;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity implements OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void changeFragment(int id) {
        if (id == 1) {
            ClassesFragment fragment = new ClassesFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.layout, fragment).commit();
        }
    }
}