package com.example.secure_notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity {

    private DB db;
    private String userEmail;
    private List<DB.Folder> allFolders;  // To store the full list of folders
    private List<DB.Folder> filteredFolders;  // To store the filtered list based on search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folders);

        db = new DB();
        userEmail = getIntent().getStringExtra("email");

        RecyclerView recyclerView = findViewById(R.id.rvFolders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));  // Set LayoutManager first

        // Call fetchFolders to set adapter once data is retrieved
        fetchFolders();

        ImageButton fabAddFolder = findViewById(R.id.fabAddFolder);
        fabAddFolder.setOnClickListener(v -> showAddFolderDialog());

        // Set up the search functionality
        EditText etSearchFolders = findViewById(R.id.etSearchFolders);
        etSearchFolders.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter the folders based on the search query
                filterFolders(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void showAddFolderDialog() {
        // Create a dialog to input folder name
        EditText folderNameEditText = new EditText(this);
        folderNameEditText.setHint("Enter folder name");

        new android.app.AlertDialog.Builder(this)
                .setTitle("Add Folder")
                .setView(folderNameEditText)
                .setPositiveButton("Add", (dialog, which) -> {
                    String folderName = folderNameEditText.getText().toString().trim();
                    if (!folderName.isEmpty()) {
                        addFolder(userEmail, folderName);  // Insert the folder into the database
                    } else {
                        Toast.makeText(FolderActivity.this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addFolder(String userEmail, String folderName) {
        db.insertFolder(userEmail, folderName, new DB.DBCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result) {
                    Toast.makeText(FolderActivity.this, "Folder added successfully", Toast.LENGTH_SHORT).show();
                    fetchFolders();  // Refresh folder list after adding
                } else {
                    Toast.makeText(FolderActivity.this, "Failed to add folder", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(FolderActivity.this, "Error adding folder: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch the list of folders and update the UI
    private void fetchFolders() {
        db.fetchFolders(userEmail, new DB.DBCallback<List<DB.Folder>>() {
            @Override
            public void onResponse(List<DB.Folder> folders) {
                Log.d("FolderActivity", "Folders fetched: " + folders);

                if (folders != null && !folders.isEmpty()) {
                    allFolders = folders;  // Store the full list of folders
                    filteredFolders = new ArrayList<>(allFolders);  // Initialize filteredFolders with all folders
                    FolderAdapter folderAdapter = new FolderAdapter(filteredFolders);
                    RecyclerView recyclerView = findViewById(R.id.rvFolders);
                    recyclerView.setAdapter(folderAdapter);  // Now set the adapter
                    recyclerView.post(() -> recyclerView.getLayoutManager().requestLayout());
                    Log.d("FolderActivity", "Adapter set with " + folders.size() + " items");
                } else {
                    Toast.makeText(FolderActivity.this, "No folders found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(FolderActivity.this, "Error fetching folders", Toast.LENGTH_SHORT).show();
                Log.e("FolderActivity", "Error fetching folders: " + t.getMessage());
            }
        });
    }

    // Filter folders based on the search query
    private void filterFolders(String query) {
        filteredFolders.clear();  // Clear the current filtered list

        if (query.isEmpty()) {
            // If the search query is empty, show all folders
            filteredFolders.addAll(allFolders);
        } else {
            // Otherwise, filter folders based on the query
            for (DB.Folder folder : allFolders) {
                if (folder.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredFolders.add(folder);  // Add matching folders
                }
            }
        }
        // Notify the adapter that the data has changed
        RecyclerView recyclerView = findViewById(R.id.rvFolders);
        FolderAdapter adapter = (FolderAdapter) recyclerView.getAdapter();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Handle folder deletion
    public void deleteFolder(int folderId) {
        db.deleteFolder(folderId, new DB.DBCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result) {
                    Toast.makeText(FolderActivity.this, "Folder deleted", Toast.LENGTH_SHORT).show();
                    fetchFolders();  // Refresh the folder list after deletion
                } else {
                    Toast.makeText(FolderActivity.this, "Failed to delete folder", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(FolderActivity.this, "Error deleting folder", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onFolderClick(int folderId) {
        Intent intent = new Intent(FolderActivity.this, NoteActivity.class);
        intent.putExtra("folderId", folderId);
        intent.putExtra("userEmail", FolderActivity.this.userEmail);  // Pass userEmail
        Log.d("FolderActivity", "Folder clicked: " + folderId);
        startActivity(intent);
    }
}

