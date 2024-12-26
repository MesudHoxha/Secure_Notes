package com.example.secure_notes;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private DB DB;
    private TextView textView;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        DB = new DB();
        textView = findViewById(R.id.appTitle);
        email = getIntent().getStringExtra("email");



        Button getStarted = findViewById(R.id.btnFolders);
        getStarted.setOnClickListener(v -> {
            DB.insertFolder(email, "Default", new DB.DBCallback<Boolean>() {
                @Override
                public void onResponse(Boolean result) {
                    if (result) {
                        Toast.makeText(HomeActivity.this, "Default Folder added successfully", Toast.LENGTH_SHORT).show();// Refresh folder list after adding
                    } else {
                        Toast.makeText(HomeActivity.this, "Failed to add  default folder", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Toast.makeText(HomeActivity.this, "Error adding folder: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
            Intent intent = new Intent(HomeActivity.this, FolderActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });


        // Fetch and display the user's first name asynchronously
        fetchAndDisplayFirstName();

        ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "translationX", -1000f, 0f);
        animator.setDuration(3000);  // Set the duration of the animation (3 second)
        animator.setInterpolator(new DecelerateInterpolator());  // Smooth animation
        animator.start();
    }

    private void fetchAndDisplayFirstName() {
        DB.getFirstName(email, new DB.DBCallback<String>() {
            @Override
            public void onResponse(String firstName) {
                runOnUiThread(() -> {
                    if (firstName != null && !firstName.isEmpty()) {
                        textView.setText("Welcome to Secure Notes, \n" + firstName);
                    } else {
                        textView.setText("Welcome to Secure Notes, \n User");
                    }
                });
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "Failed to fetch user data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    textView.setText("Welcome, User");
                });
            }
        });
    }
}
