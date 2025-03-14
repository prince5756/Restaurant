package com.example.user_restaurant;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.user_restaurant.activities.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;


public class UserDoesNotLogIn {
    public static boolean checkUserLogin(Context context) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // If the user is not logged in
        if (auth.getCurrentUser() == null) {
            Toast.makeText(context, "User does not login", Toast.LENGTH_SHORT).show();

            // Redirect to LoginActivity
            context.startActivity(new Intent(context, LoginActivity.class));
        }
        return false;
    }



}
