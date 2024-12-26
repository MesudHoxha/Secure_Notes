package com.example.secure_notes;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

import android.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;



public class VerifyOtpSignUpActivity extends AppCompatActivity {
    private EditText otpEditText;
    private String generatedOtp, firstName, lastName, email, password;
    private DB DB;
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
        DB = new DB();

        generatedOtp = generateOtp();
        firstName = getIntent().getStringExtra("firstName");
        lastName = getIntent().getStringExtra("lastName");
        email = getIntent().getStringExtra("email");
        password = getIntent().getStringExtra("password");

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
            // Fetch the master key from the server
            DB.getMasterKey(new DB.DBCallback<DB.MasterKeyResponse>() {
                @Override
                public void onResponse(DB.MasterKeyResponse response) {
                    if (response != null) {
                        try {
                            // Retrieve the master key
                            String base64MasterKey = response.getKeyValue();

                            // Generate the user key
                            byte[] userKey = generateUserKey();
                            String key = userKey.toString();
                            Log.d("Generated Key: ", "Key" + key);
                            // Encrypt the user key with the master key
                            String encryptedUserKey = encryptUserKey(userKey, base64MasterKey);
                            Log.d("Encryoted Key: ", "Key" + encryptedUserKey);
                            // Proceed with user registration
                            DB.insertData(firstName, lastName, email, password, encryptedUserKey, new DB.DBCallback<Boolean>() {
                                @Override
                                public void onResponse(Boolean success) {
                                    runOnUiThread(() -> {
                                        if (success) {
                                            Toast.makeText(VerifyOtpSignUpActivity.this, "OTP verified successfully. Your account is now created.", Toast.LENGTH_SHORT).show();
                                            if (countDownTimer != null) {
                                                countDownTimer.cancel();
                                            }
                                            Intent intent = new Intent(VerifyOtpSignUpActivity.this, HomeActivity.class);
                                            intent.putExtra("email", email);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(VerifyOtpSignUpActivity.this, "An error occurred while creating your account. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Throwable t) {
                                    runOnUiThread(() -> Toast.makeText(VerifyOtpSignUpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            });
                        } catch (Exception e) {
                            Toast.makeText(VerifyOtpSignUpActivity.this, "Error encrypting user key: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(VerifyOtpSignUpActivity.this, "Failed to retrieve master key.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(VerifyOtpSignUpActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
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
        countDownTimer = new CountDownTimer(60000, 1000) { // 60 seconds, update every 1 second
            @Override
            public void onTick(long millisUntilFinished) {
                timerTextView.setText("OTP is valuable for the next " + millisUntilFinished / 1000 + "s");
            }

            @Override
            public void onFinish() {
                timerTextView.setText("OTP expired! Resending...");
                resendCode(email);
            }
        };
        countDownTimer.start();
    }

    private void sendEmail(String email, String otp) {
        new Thread(() -> {
            if (OTPSender.sendEmail(email, otp)) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
                    startTimerOTP(email);
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

    private byte[] generateUserKey() {
        byte[] userKey = new byte[32]; // 256-bit key for AES
        new SecureRandom().nextBytes(userKey);
        return userKey;
    }
    private String encryptUserKey(byte[] userKey, String base64MasterKey) throws Exception {
        // Decode the master key from Base64
        byte[] masterKey = Base64.decode(base64MasterKey, Base64.DEFAULT);

        // Create an AES Cipher
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(masterKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        // Encrypt the user key
        byte[] encryptedKey = cipher.doFinal(userKey);

        // Encode the encrypted key to Base64 for safe storage/transmission
        return Base64.encodeToString(encryptedKey, Base64.DEFAULT);
    }
}
