package com.example.secure_notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.CountDownTimer;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class VerifyOtpLoginActivity extends AppCompatActivity {
    private EditText otpEditText;
    private String generatedOtp,  email;
    private CountDownTimer countDownTimer;
    private TextView timerTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        otpEditText = findViewById(R.id.editTextOTP);
        Button verifyButton = findViewById(R.id.verifyButton);
        Button resendButton = findViewById(R.id.resendButton);
        timerTextView = findViewById(R.id.timerTextView);


        generatedOtp = generateOtp();
        email = getIntent().getStringExtra("email");

        sendEmail(email, generatedOtp);

        verifyButton.setOnClickListener(v -> {
            String enteredOtp = otpEditText.getText().toString().trim();
            verify(enteredOtp, generatedOtp);
        });

        resendButton.setOnClickListener(v -> resendCode(email));
    }



    public void verify(String enteredOtp, String generatedOtp) {
        if (TextUtils.isEmpty(enteredOtp)) {
            Toast.makeText(this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (enteredOtp.equals(generatedOtp)) {
                Toast.makeText(this, "OTP verified successfully. Your are logged in.", Toast.LENGTH_SHORT).show();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                Intent intent = new Intent(VerifyOtpLoginActivity.this, FolderActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
        } else {
            Toast.makeText(this, "Invalid OTP. Please check your email and try again.", Toast.LENGTH_SHORT).show();
        }

    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = random.nextInt(900000) + 100000;
        return String.valueOf(otp);
    }

    public void startTimerOTP(String email) {
        // Create a CountDownTimer instance
        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds, update every 1 second

            @Override
            public void onTick(long millisUntilFinished) {
                // Update the timer TextView
                timerTextView.setText("OTP is valuable for next "+ millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                // Timer finished
                timerTextView.setText("OTP expired! Resending...");
                resendCode(email);

            }
        };

        // Start the timer
        countDownTimer.start();

    }

    private void sendEmail(String email, String otp) {
        new Thread(() -> {
            if (OTPSender.sendEmail(email, otp)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                    startTimerOTP(email); // Ensure this runs on the main thread
                });
            } else {
                runOnUiThread(() -> Toast.makeText(this, "Failed to send OTP. Please try again.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public void resendCode(String email) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerTextView.setText("OTP expired! Resending...");
        generatedOtp = generateOtp();
        sendEmail(email, generatedOtp);

    }


}