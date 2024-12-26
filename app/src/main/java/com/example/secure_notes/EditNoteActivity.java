package com.example.secure_notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditNoteActivity extends AppCompatActivity {

    private EditText etNoteTitle, etNoteContent;
    private Button btnSaveNote;
    private DB db;
    private int noteId = -1, folderId;// Default value for new note

    private String userEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

        db = new DB();
        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);

        // Get the noteId, userEmail, and folderId from the intent
        noteId = getIntent().getIntExtra("noteId", -1);
        Log.d("ID-edit","Id:" + noteId);
        userEmail = getIntent().getStringExtra("userEmail");
        folderId = getIntent().getIntExtra("folderId", -1);

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> {
            // Passing an empty noteId (-1) to indicate that we are adding a new note
            Intent intent = new Intent(EditNoteActivity.this, NoteActivity.class);
            intent.putExtra("userEmail", userEmail);
            intent.putExtra("folderId", folderId);
            startActivity(intent);
        });
        // If it's an existing note, fetch the data and populate the fields
        if (noteId != -1) {
            fetchNoteData(noteId);


        }

        btnSaveNote.setOnClickListener(v -> {
            String title = etNoteTitle.getText().toString().trim();
            String content = etNoteContent.getText().toString().trim();
            Log.d("Test", "Test note: " + title + " " + content + " " + userEmail + " " + folderId);
            // Validate the fields
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(content)) {
                Toast.makeText(this, "Title and content cannot be empty", Toast.LENGTH_SHORT).show();
            } else {
                if (noteId == -1) {
                    // If it's a new note, insert the note into the database
                    insertNewNote(title, content, userEmail, folderId);
                } else {
                    // If it's an existing note, update the note
                    updateNoteData(noteId, title, content, userEmail, folderId);
                    Log.d("Update Note", "Details: " + noteId + " " + title + " " + content + " " + userEmail + " " + folderId) ;
                }
            }
        });
    }

    private void insertNewNote(String title, String content, String userEmail, int folderId) {
        db.insertNote(title, content, userEmail, folderId, new DB.DBCallback<Boolean>() {
            @Override
            public void onResponse(Boolean success) {
                if (success) {
                    Toast.makeText(EditNoteActivity.this, "Note added successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditNoteActivity.this, NoteActivity.class);
                    intent.putExtra("userEmail", userEmail);  // Pass userEmail
                    intent.putExtra("folderId", folderId);
                    startActivity(intent);
                    finish();  // Close the activity and return
                } else {
                    Toast.makeText(EditNoteActivity.this, "Failed to add note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(EditNoteActivity.this, "Error adding note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateNoteData(int noteId, String title, String content, String userEmail, int folderId) {
        db.updateNote(noteId, title, content, userEmail, folderId, new DB.DBCallback<Boolean>() {
            @Override
            public void onResponse(Boolean success) {
                if (success) {
                    Toast.makeText(EditNoteActivity.this, "Note updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditNoteActivity.this, NoteActivity.class);
                    intent.putExtra("userEmail", userEmail);  // Pass userEmail
                    intent.putExtra("folderId", folderId);
                    startActivity(intent);
                    finish();  // Close the activity and return
                } else {
                    Toast.makeText(EditNoteActivity.this, "Failed to update note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(EditNoteActivity.this, "Error updating note", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchNoteData(int noteId) {
        db.getNoteById(noteId, new DB.DBCallback<DB.Note>() {
            @Override
            public void onResponse(DB.Note note) {
                if (note != null) {
                    // Populate the EditText fields with the note data
                    etNoteTitle.setText(note.getTitle());
                    etNoteContent.setText(note.getNoteText());
                } else {
                    // If no note is found, show a message
                    Toast.makeText(EditNoteActivity.this, "Note not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                // Handle the failure scenario
                Toast.makeText(EditNoteActivity.this, "Error fetching note data", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
