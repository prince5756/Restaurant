package com.example.user_restaurant.activities;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.user_restaurant.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class PaymentMethods extends AppCompatActivity implements PaymentResultListener {
    private String restaurantUid, selectedPlan, paymentAmount;
    private DocumentReference restaurantDocRef;
    final long[] vibe = {0, 500};
    final Uri notificationsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_methods); // Ensure correct layout file

        restaurantUid = getIntent().getStringExtra("restaurantUid");
        String amount = getIntent().getStringExtra("amount").trim();

        // Determine selected plan
        if (amount.equals("599.00")) {
            selectedPlan = "monthly";
        } else if (amount.equals("5499.00")) {
            selectedPlan = "yearly";
        } else if (amount.equals("25999.00")) {
            selectedPlan = "permanent";
        } else {
            selectedPlan = "unknown";
        }
        paymentAmount = amount;
        restaurantDocRef = FirebaseFirestore.getInstance()
                .collection("RestaurantInformation")
                .document(restaurantUid);

        startPayment(amount);
    }

    private void startPayment(String amount) {
        Checkout checkout = new Checkout();
        checkout.setKeyID(getString(R.string.razorpay_key));

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Quick Reserve");
            options.put("description", "Subscription Payment");
            options.put("currency", "INR");
            options.put("amount", convertAmount(amount));
            options.put("prefill.email", "QuickReserve@example.com");
            options.put("prefill.contact", "7383204880");

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error initiating payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private int convertAmount(String amount) {
        amount = amount.replace("₹", "").trim();
        double amt = Double.parseDouble(amount);
        return (int) (amt * 100);
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        // Update payment details first
        updatePaymentDetails();

        // Show notification
        showPaymentNotification();

        // Navigate to success
        Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, SuccessActivity.class));
        finish();
    }

    private void showPaymentNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "payment_channel_id",
                    "Payment Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifies about payment status");
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "payment_channel_id")
                .setSmallIcon(R.drawable.vec_logo)
                .setContentTitle("Payment Successful")
                .setContentText("You are now a premium member!")
                .setSound(notificationsound)
                .setVibrate(vibe)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(1001, builder.build());
        }
    }

    private void updatePaymentDetails() {
        String paymentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String paymentValidity = calculateValidity();

        HashMap<String, Object> updates = new HashMap<>();
        updates.put("isPayment", true);
        updates.put("paymentDate", paymentDate);
        updates.put("paymentValidity", paymentValidity);
        updates.put("paymentAmount", paymentAmount);
        updates.put("paymentStatus", "paid");

        restaurantDocRef.update(updates)
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String calculateValidity() {
        Calendar calendar = Calendar.getInstance();
        switch (selectedPlan) {
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.getTime());
            case "yearly":
                calendar.add(Calendar.YEAR, 1);
                return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(calendar.getTime());
            case "permanent":
                return "Permanent";
            default:
                return "N/A";
        }
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
        finish();
    }
}