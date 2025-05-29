package com.example.user_restaurant.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.user_restaurant.R;

public class SuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        TextView textView = findViewById(R.id.successMessage);
        Button  btnHome = findViewById(R.id.btnHome);

        textView.setText("Payment Successful! 🎉 \nYou are Now member! ");



        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(SuccessActivity.this, MainActivity.class)); // Redirect to Success Page

            }
        });




    }
}
