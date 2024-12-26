package com.example.secure_notes;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {

    private List<DB.Folder> folderList;

    // Constructor
    public FolderAdapter(List<DB.Folder> folderList) {
        this.folderList = folderList;
        Log.d("FolderAdapter", "Folder list size: " + folderList.size());
    }

    @Override
    public FolderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FolderViewHolder holder, int position) {
        DB.Folder folder = folderList.get(position);
        Log.d("FolderAdapter", "Binding folder: " + folder.getName());
        holder.folderName.setText(folder.getName());

        // Set up the delete button click event
        holder.deleteButton.setOnClickListener(v -> {
            // Pass the folder ID to FolderActivity to handle deletion
            ((FolderActivity) holder.itemView.getContext()).deleteFolder(folder.getId());
        });

        holder.itemView.setOnClickListener(v -> {
            ((FolderActivity) holder.itemView.getContext()).onFolderClick(folder.getId());   // Navigate to NoteActivity with folderId
        });
    }

    @Override
    public int getItemCount() {
        return folderList.size();
    }

    // ViewHolder to hold folder item views
    public static class FolderViewHolder extends RecyclerView.ViewHolder {
        TextView folderName;
        ImageButton deleteButton;

        public FolderViewHolder(View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            deleteButton = itemView.findViewById(R.id.btnDeleteFolder);
        }
    }
}
