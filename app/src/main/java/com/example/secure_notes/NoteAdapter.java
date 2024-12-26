package com.example.secure_notes;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private Context context;
    private List<DB.Note> notes;

    // Constructor accepting the list of notes
    public NoteAdapter(Context context ,List<DB.Note> notes) {
        this.context = context;
        this.notes = notes;
    }

    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the note_item layout for each note
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        DB.Note note = notes.get(position);

        // Set the title and content for each note
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getNoteText());

        holder.deleteButton.setOnClickListener(v -> {
            // Pass the folder ID to FolderActivity to handle deletion
            ((NoteActivity) holder.itemView.getContext()).deleteNote(note.getId());
        });

        holder.itemView.setOnClickListener(v -> {
            // Pass the noteId to the edit activity
            Intent intent = new Intent(context, EditNoteActivity.class);
            intent.putExtra("noteId", note.getId());
            Log.d("ID","Id: " + note.getId());
            intent.putExtra("userEmail", note.getUserEmail());  // Pass userEmail
            intent.putExtra("folderId", note.getFolderId());
            context.startActivity(intent);
        });// Bind noteText (the content) to the TextView
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    // ViewHolder class to hold the views for each note item
    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView contentTextView;
        ImageButton deleteButton;

        public NoteViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textNoteTitle);
            contentTextView = itemView.findViewById(R.id.textNoteContent);
            deleteButton = itemView.findViewById(R.id.btnDeleteNote);
        }
    }
}
