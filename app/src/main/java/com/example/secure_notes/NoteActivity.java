package com.example.secure_notes;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NoteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private DB db;
    private int folderId;

    private String userEmail;
    private EditText etSearchNotes;

    private List<DB.Note> allNotes;  // The full list of notes
    private List<DB.Note> filteredNotes;  // The filtered list based on search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        db = new DB();  // Initialize DB object
        recyclerView = findViewById(R.id.rvNotes);

        // Get the folder ID from the intent
        folderId = getIntent().getIntExtra("folderId", -1);
        userEmail = getIntent().getStringExtra("userEmail");

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize search EditText
        etSearchNotes = findViewById(R.id.etSearchNotes);

        // Fetch notes associated with the folder
        fetchNotes();

        ImageButton backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> {
            // Passing an empty noteId (-1) to indicate that we are adding a new note
            Intent intent = new Intent(NoteActivity.this, FolderActivity.class);
            intent.putExtra("email", userEmail);  // Pass userEmail
            startActivity(intent);
        });

        // Search functionality: Listen to search query changes
        etSearchNotes.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter the notes based on the search query
                filterNotes(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Floating Action Button for adding a new note
        ImageButton fabAddNote = findViewById(R.id.fabAddNote);
        fabAddNote.setOnClickListener(v -> {
            // Passing an empty noteId (-1) to indicate that we are adding a new note
            Intent intent = new Intent(NoteActivity.this, EditNoteActivity.class);
            intent.putExtra("userEmail", userEmail);  // Pass userEmail
            intent.putExtra("folderId", folderId);
            intent.putExtra("noteId", -1);  // Pass -1 for a new note
            startActivity(intent);
        });
    }

    private void fetchNotes() {
        db.fetchNotesByFolderId(folderId, new DB.DBCallback<List<DB.Note>>() {
            @Override
            public void onResponse(List<DB.Note> notes) {
                if (notes != null && !notes.isEmpty()) {
                    allNotes = notes;  // Store the full list of notes
                    filteredNotes = new ArrayList<>(allNotes);  // Initialize filteredNotes with all notes
                    noteAdapter = new NoteAdapter(NoteActivity.this, filteredNotes);
                    recyclerView.setAdapter(noteAdapter);  // Set up the RecyclerView adapter
                } else {
                    Toast.makeText(NoteActivity.this, "No notes found for this folder", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(NoteActivity.this, "No notes found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Filter notes based on the search query
    private void filterNotes(String query) {
        filteredNotes.clear();  // Clear the current filtered list

        if (query.isEmpty()) {
            // If the search query is empty, show all notes
            filteredNotes.addAll(allNotes);
        } else {
            // Otherwise, filter notes based on the query
            for (DB.Note note : allNotes) {
                if (note.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        note.getNoteText().toLowerCase().contains(query.toLowerCase())) {
                    filteredNotes.add(note);  // Add matching notes
                }
            }
        }
        noteAdapter.notifyDataSetChanged();  // Notify the adapter that the data has changed
    }

    public void deleteNote(int noteId) {
        db.deleteNote(noteId, new DB.DBCallback<Boolean>() {
            @Override
            public void onResponse(Boolean result) {
                if (result) {
                    Toast.makeText(NoteActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
                    fetchNotes();  // Refresh the folder list after deletion
                } else {
                    Toast.makeText(NoteActivity.this, "Failed to delete note", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(NoteActivity.this, "Error deleting note", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

