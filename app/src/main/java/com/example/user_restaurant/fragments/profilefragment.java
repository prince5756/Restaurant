package com.example.user_restaurant.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user_restaurant.R;
import com.example.user_restaurant.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;


public class profilefragment extends Fragment {
    Button btnLogOut;
    FirebaseAuth auth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profilefragment, container, false);
        btnLogOut=view.findViewById(R.id.btnLogOut);
        auth=FirebaseAuth.getInstance();
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                auth.signOut();
            }
        });
        return view;
    }
}