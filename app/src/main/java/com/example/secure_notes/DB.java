package com.example.secure_notes;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.*;
import retrofit2.converter.gson.GsonConverterFactory;
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DB {

    private ApiService apiService;

    // Constructor to initialize Retrofit
    public DB() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2/rest-api-enc/api/")  // Replace with actual API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // Check if an email exists in the database
    public void checkEmail(String email, final DBCallback<Boolean> callback) {
        apiService.getUser(email).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getEmail() != null) {
                    callback.onResponse(true);  // Email exists
                } else {
                    callback.onResponse(false); // Email does not exist
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("DB", "Error checking email: " + t.getMessage());
                callback.onFailure(t); // Propagate failure
            }
        });
    }

    // Insert a new user into the database
    public void insertData(String firstName, String lastName, String email, String password, String userEncryptionKey, final DBCallback<Boolean> callback) {
        RegisterRequest registerRequest = new RegisterRequest(firstName, lastName, email, password, userEncryptionKey);
        apiService.register(registerRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(true); // Registration successful
                } else {
                    callback.onResponse(false); // Registration failed
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("DB", "Error inserting data: " + t.getMessage());
                callback.onFailure(t); // Propagate failure
            }
        });
    }

    // Retrieve the first name of a user by email
    public void getFirstName(String email, final DBCallback<String> callback) {
        apiService.getUser(email).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body().getFirstName()); // Return first name
                } else {
                    callback.onResponse(null); // User not found
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Log.e("DB", "Error getting first name: " + t.getMessage());
                callback.onFailure(t); // Propagate failure
            }
        });
    }

    // Validate user login by checking email and password
    public void validateUser(String email, String password, final DBCallback<Boolean> callback) {
        LoginRequest loginRequest = new LoginRequest(email, password);
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Check the response message for login success or failure
                    if (response.body().getMessage().equals("Login successful")) {
                        callback.onResponse(true);  // Successful login
                    } else {
                        callback.onResponse(false);  // Invalid login
                    }
                } else {
                    callback.onResponse(false);  // Invalid response or server error
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("DB", "Error validating user: " + t.getMessage());
                callback.onFailure(t);  // Propagate failure
            }
        });
    }

    // Insert a new folder into the database
    public void insertFolder(String userEmail, String folderName, final DBCallback<Boolean> callback) {
        InsertFolderRequest insertFolderRequest = new InsertFolderRequest(userEmail, folderName);
        apiService.insertFolder(insertFolderRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(true); // Folder inserted successfully
                } else {
                    callback.onResponse(false); // Folder insertion failed
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("DB", "Error inserting folder: " + t.getMessage());
                callback.onFailure(t); // Propagate failure
            }
        });
    }

    // Insert a new note for the user


    public void fetchFolders(String userEmail, final DBCallback<List<Folder>> callback) {
        apiService.getFolders(userEmail).enqueue(new Callback<List<Folder>>() {
            @Override
            public void onResponse(Call<List<Folder>> call, Response<List<Folder>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body());  // Pass the list of folders
                } else {
                    callback.onResponse(null);  // No folders found
                }
            }

            @Override
            public void onFailure(Call<List<Folder>> call, Throwable t) {
                Log.e("DB", "Error fetching folders: " + t.getMessage());
                callback.onFailure(t);  // Propagate failure
            }
        });
    }

    public void deleteFolder(int folderId, final DBCallback<Boolean> callback) {
        // Create a DeleteFolderRequest with folderId
        DeleteFolderRequest deleteFolderRequest = new DeleteFolderRequest(folderId);
        Log.d("Folder deleted", "Id: " + folderId);

        // Make the API call to delete the folder
        apiService.deleteFolder(deleteFolderRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(true);  // Folder deleted successfully
                } else {
                    callback.onResponse(false);  // Deletion failed
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("DB", "Error deleting folder: " + t.getMessage());
                callback.onFailure(t);  // Propagate failure
            }
        });
    }

    public void fetchNotesByFolderId(int folderId, final DBCallback<List<Note>> callback) {
        apiService.getNotesByFolderId(folderId).enqueue(new Callback<List<Note>>() {
            @Override
            public void onResponse(Call<List<Note>> call, Response<List<Note>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Note> encryptedNotes = response.body(); // Retrieved list of notes

                    // Fetch the master key
                    getMasterKey(new DBCallback<MasterKeyResponse>() {
                        @Override
                        public void onResponse(MasterKeyResponse masterKeyResponse) {
                            if (masterKeyResponse != null && masterKeyResponse.getKeyValue() != null) {
                                String base64MasterKey = masterKeyResponse.getKeyValue();

                                // Process and decrypt each note
                                List<Note> decryptedNotes = new ArrayList<>();
                                for (Note note : encryptedNotes) {
                                    try {
                                        // Fetch the user's encryption key for each note
                                        getUserEncryptionKey(note.getUserEmail(), new DBCallback<String>() {
                                            @Override
                                            public void onResponse(String encryptedUserKey) {
                                                try {
                                                    // Decrypt the user-specific key
                                                    byte[] userKey = decryptUserKey(encryptedUserKey, base64MasterKey);
                                                    Log.d("fetchNotes", "Decrypted key: " + userKey.toString());
                                                    // Decrypt the note content
                                                    String decryptedContent = decryptContent(note.getNoteText(), userKey);
                                                    Log.d("fetchNotes", "Decrypted content: " + decryptedContent);
                                                    note.setNoteText(decryptedContent);

                                                    // Decrypt the note description if it exists

                                                    // Add the decrypted note to the list
                                                    decryptedNotes.add(note);
                                                    Log.d("Decrypted notes", "Notes: "+ decryptedNotes);
                                                    // If all notes are processed, pass the list to the callback
                                                    if (decryptedNotes.size() == encryptedNotes.size()) {

                                                        callback.onResponse(decryptedNotes);
                                                    }
                                                } catch (Exception e) {
                                                    callback.onFailure(e); // Handle decryption errors
                                                }
                                            }

                                            @Override
                                            public void onFailure(Throwable t) {
                                                callback.onFailure(t); // Handle failure to fetch user key
                                            }
                                        });
                                    } catch (Exception e) {
                                        callback.onFailure(e); // Handle errors in the decryption loop
                                    }
                                }
                            } else {
                                callback.onFailure(new Exception("Failed to retrieve master key"));
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            callback.onFailure(t); // Handle failure to fetch master key
                        }
                    });
                } else {
                    callback.onResponse(null); // No notes found or error in response
                }
            }

            @Override
            public void onFailure(Call<List<Note>> call, Throwable t) {
                callback.onFailure(t); // Handle the failure
            }
        });
    }

    public void getUserEncryptionKey(String userEmail, final DBCallback<String> callback) {
        apiService.getUser(userEmail).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body().getUserEncryptionKey());
                } else {
                    callback.onResponse(null); // User key not found
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                callback.onFailure(t); // Handle failure
            }
        });
    }
    public void insertNote(String title, String content, String userEmail, int folderId, final DBCallback<Boolean> callback) {
        // Step 1: Fetch the master key
        getMasterKey(new DBCallback<MasterKeyResponse>() {
            @Override
            public void onResponse(MasterKeyResponse masterKeyResponse) {
                if (masterKeyResponse != null && masterKeyResponse.getKeyValue() != null) {
                    String base64MasterKey = masterKeyResponse.getKeyValue();

                    // Step 2: Fetch the user's encryption key
                    getUserEncryptionKey(userEmail, new DBCallback<String>() {
                        @Override
                        public void onResponse(String encryptedUserKey) {
                            try {
                                // Step 3: Decrypt the user key with the master key
                                byte[] userKey = decryptUserKey(encryptedUserKey, base64MasterKey);

                                // Step 4: Encrypt the note content
                                String encryptedContent = encryptContent(content, userKey);

                                // Step 5: Perform the insert operation with encrypted content
                                NoteRequest noteRequest = new NoteRequest(title, encryptedContent, userEmail, folderId);
                                apiService.insertNote(noteRequest).enqueue(new Callback<ApiResponse>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            callback.onResponse(true); // Success
                                        } else {
                                            callback.onResponse(false); // Failure
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                                        callback.onFailure(t); // Handle failure
                                    }
                                });
                            } catch (Exception e) {
                                callback.onFailure(e); // Handle encryption failure
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            callback.onFailure(t); // Handle failure to fetch user key
                        }
                    });
                } else {
                    callback.onFailure(new Exception("Failed to retrieve master key"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t); // Handle failure to fetch master key
            }
        });
    }


    public void updateNote(int noteId, String title, String content, String userEmail, int folderId, final DBCallback<Boolean> callback) {
        // Step 1: Fetch the master key
        getMasterKey(new DBCallback<MasterKeyResponse>() {
            @Override
            public void onResponse(MasterKeyResponse masterKeyResponse) {
                if (masterKeyResponse != null && masterKeyResponse.getKeyValue() != null) {
                    String base64MasterKey = masterKeyResponse.getKeyValue();

                    // Step 2: Fetch the user's encryption key
                    getUserEncryptionKey(userEmail, new DBCallback<String>() {
                        @Override
                        public void onResponse(String encryptedUserKey) {
                            try {
                                // Step 3: Decrypt the user key with the master key
                                byte[] userKey = decryptUserKey(encryptedUserKey, base64MasterKey);

                                // Step 4: Encrypt the note content
                                String encryptedContent = encryptContent(content, userKey);

                                // Step 5: Perform the update operation with encrypted content
                                NoteRequest noteRequest = new NoteRequest(title, encryptedContent, userEmail, folderId);
                                apiService.updateNote(noteId, noteRequest).enqueue(new Callback<ApiResponse>() {
                                    @Override
                                    public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            callback.onResponse(true); // Success
                                        } else {
                                            callback.onResponse(false); // Failure
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<ApiResponse> call, Throwable t) {
                                        callback.onFailure(t); // Handle failure
                                    }
                                });
                            } catch (Exception e) {
                                callback.onFailure(e); // Handle encryption failure
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            callback.onFailure(t); // Handle failure to fetch user key
                        }
                    });
                } else {
                    callback.onFailure(new Exception("Failed to retrieve master key"));
                }
            }

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t); // Handle failure to fetch master key
            }
        });
    }



    public void getNoteById(int noteId, final DBCallback<Note> callback) {
        // Fetch the note details from the server
        apiService.getNoteById(noteId).enqueue(new Callback<Note>() {
            @Override
            public void onResponse(Call<Note> call, Response<Note> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Note note = response.body();  // Fetched note from server

                    // Fetch the master key
                    getMasterKey(new DBCallback<MasterKeyResponse>() {
                        @Override
                        public void onResponse(MasterKeyResponse masterKeyResponse) {
                            if (masterKeyResponse != null && masterKeyResponse.getKeyValue() != null) {
                                String base64MasterKey = masterKeyResponse.getKeyValue();

                                // Fetch the user's encryption key
                                getUserEncryptionKey(note.getUserEmail(), new DBCallback<String>() {
                                    @Override
                                    public void onResponse(String encryptedUserKey) {
                                        try {
                                            // Decrypt the user-specific key with the master key
                                            byte[] userKey = decryptUserKey(encryptedUserKey, base64MasterKey);

                                            // Decrypt the note content
                                            String decryptedContent = decryptContent(note.getNoteText(), userKey);
                                            note.setNoteText(decryptedContent);


                                            // Pass the decrypted note to the callback
                                            callback.onResponse(note);
                                        } catch (Exception e) {
                                            callback.onFailure(e);  // Handle decryption errors
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        callback.onFailure(t);  // Handle failure to fetch user key
                                    }
                                });
                            } else {
                                callback.onFailure(new Exception("Failed to retrieve master key"));
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            callback.onFailure(t);  // Handle failure to fetch master key
                        }
                    });
                } else {
                    callback.onResponse(null);  // No note found
                    Log.d("DB", "No note found for ID: " + noteId);
                }
            }

            @Override
            public void onFailure(Call<Note> call, Throwable t) {
                callback.onFailure(t);  // Handle network or server failure
                Log.e("DB", "Error fetching note: " + t.getMessage());
            }
        });
    }


    public void deleteNote(int noteId, final DBCallback<Boolean> callback) {
        // Create a DeleteFolderRequest with folderId
        DeleteNoteRequest deleteNoteRequest = new DeleteNoteRequest(noteId);

        // Make the API call to delete the folder
        apiService.deleteNote(deleteNoteRequest).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(true);  // Folder deleted successfully
                } else {
                    callback.onResponse(false);  // Deletion failed
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("DB", "Error deleting folder: " + t.getMessage());
                callback.onFailure(t);  // Propagate failure
            }
        });
    }
    public void getMasterKey(final DBCallback<MasterKeyResponse> callback) {
        apiService.getMasterKey().enqueue(new Callback<MasterKeyResponse>() {
            @Override
            public void onResponse(Call<MasterKeyResponse> call, Response<MasterKeyResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResponse(response.body()); // Pass the master key response to the callback
                } else {
                    callback.onResponse(null); // Handle unsuccessful response
                }
            }

            @Override
            public void onFailure(Call<MasterKeyResponse> call, Throwable t) {
                callback.onFailure(t); // Pass the failure to the callback
            }
        });
    }


    private String encryptContent(String content, byte[] userKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(userKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedData = cipher.doFinal(content.getBytes());
        return Base64.encodeToString(encryptedData, Base64.DEFAULT);
    }

    // Decryption method
    private String decryptContent(String encryptedContent, byte[] userKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(userKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedData = cipher.doFinal(Base64.decode(encryptedContent, Base64.DEFAULT));
        return new String(decryptedData);
    }

    // Decrypt the user key with the master key
    private byte[] decryptUserKey(String encryptedUserKey, String base64MasterKey) throws Exception {
        byte[] masterKey = Base64.decode(base64MasterKey, Base64.DEFAULT);
        byte[] encryptedKeyBytes = Base64.decode(encryptedUserKey, Base64.DEFAULT);

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec masterKeySpec = new SecretKeySpec(masterKey, "AES");
        cipher.init(Cipher.DECRYPT_MODE, masterKeySpec);

        return cipher.doFinal(encryptedKeyBytes);
    }
    public String cleanUserKey(String userKey) {
        if (userKey != null && userKey.length() > 2) {
            return userKey.substring(0, userKey.length() - 2); // Remove the last two characters
        }
        return userKey; // Return as is if length is less than or equal to 2
    }




    // Callback interface for asynchronous operations
    public interface DBCallback<T> {
        void onResponse(T result);
        void onFailure(Throwable t);
    }

    // Retrofit API Service interface
    private interface ApiService {

        // Register a new user
        @POST("register.php")
        Call<ApiResponse> register(@Body RegisterRequest request);

        // Login a user
        @POST("login.php")
        Call<LoginResponse> login(@Body LoginRequest request);
        @POST("insert_note.php")
        Call<ApiResponse> insertNote(@Body NoteRequest noteRequest);  // Pass the NoteRequest object with userEmail and folderId

        // Update an existing note
        @POST("update_note.php")
        Call<ApiResponse> updateNote(@Query("noteId") int noteId, @Body NoteRequest noteRequest);
        // Get user data by email
        @GET("user.php")
        Call<UserResponse> getUser(@Query("email") String email);

        @GET("get_folders.php")
        Call<List<Folder>> getFolders(@Query("userEmail") String userEmail);

        @POST("delete_folder.php")
        Call<ApiResponse> deleteFolder(@Body DeleteFolderRequest request);

        @POST("delete_note.php")
        Call<ApiResponse> deleteNote(@Body DeleteNoteRequest request);

        @POST("insert_folder.php")
        Call<ApiResponse> insertFolder(@Body InsertFolderRequest request);
        @GET("get_notes.php")
        Call<List<Note>> getNotesByFolderId(@Query("folderId") int folderId);

        @GET("get_notes_by_id.php")
        Call<Note> getNoteById(@Query("noteId") int noteId);

        @GET("master_key.php")
        Call<MasterKeyResponse> getMasterKey();
    }

    // Request model for inserting a folder
    private static class InsertFolderRequest {
        private String userEmail;
        private String folderName;

        public InsertFolderRequest(String userEmail, String folderName) {
            this.userEmail = userEmail;
            this.folderName = folderName;
        }
    }

    public class MasterKeyResponse {
        private String key_value;

        public String getKeyValue() {
            return key_value;
        }

        public void setKeyValue(String keyValue) {
            this.key_value = keyValue;
        }
    }
    private static class DeleteFolderRequest {
        private int folderId;

        public DeleteFolderRequest(int folderId) {
            this.folderId = folderId;
        }
    }
    private static class DeleteNoteRequest {
        private int noteId;

        public DeleteNoteRequest(int noteId) {
            this.noteId = noteId;
        }
    }
    public class NoteRequest {

        private String title;
        private String content;
        private String userEmail;  // Added field for user email
        private int folderId;      // Added field for folder ID

        // Constructor
        public NoteRequest(String title, String content, String userEmail, int folderId) {
            this.title = title;
            this.content = content;
            this.userEmail = userEmail;
            this.folderId = folderId;
        }

        // Getters and Setters
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public int getFolderId() {
            return folderId;
        }

        public void setFolderId(int folderId) {
            this.folderId = folderId;
        }
    }

    public class Note {

        private int id;
        private String user_email;
        private String title;
        private String note_text;
        private int folder_id;
        private String created_at;
        private String updated_at;

        // Constructor
        public Note(int id, String userEmail, String title, String note_text, int folderId, String createdAt, String updatedAt) {
            this.id = id;
            this.user_email = userEmail;
            this.title = title;
            this.note_text = note_text;
            this.folder_id = folderId;
            this.created_at = createdAt;
            this.updated_at = updatedAt;
        }

        // Getters and Setters
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUserEmail() {
            return user_email;
        }

        public void setUserEmail(String user_email) {
            this.user_email = user_email;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getNoteText() {
            return note_text;
        }

        public void setNoteText(String note_text) {
            this.note_text = note_text;
        }

        public int getFolderId() {
            return folder_id;
        }

        public void setFolderId(int folder_id) {
            this.folder_id = folder_id;
        }

        public String getCreatedAt() {
            return created_at;
        }

        public void setCreatedAt(String created_t) {
            this.created_at = created_at;
        }

        public String getUpdatedAt() {
            return updated_at;
        }

        public void setUpdatedAt(String updated_t) {
            this.updated_at = updated_at;
        }
    }

    // Request model for inserting a note
    private static class InsertNoteRequest {
        private String userEmail;
        private String title;
        private String noteText;
        private int folderId;

        public InsertNoteRequest(String userEmail, String title, String noteText, int folderId) {
            this.userEmail = userEmail;
            this.title = title;
            this.noteText = noteText;
            this.folderId = folderId;
        }
    }

    // Request model for registering a user
    private static class RegisterRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String password;

        private String userEncryptionKey;

        public RegisterRequest(String firstName, String lastName, String email, String password, String userEncryptionKey) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.password = password;
            this.userEncryptionKey = userEncryptionKey;
        }
    }

    public class Folder {
        private int id;
        private String name;

        // Getter and setter methods
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    // Request model for logging in a user
    private static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    // Response model for a generic API response
    private static class ApiResponse {
        private String message;

        public String getMessage() {
            return message;
        }
    }

    // Response model for a login operation
    private static class LoginResponse {
        private String message;
        private User user;

        public User getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        private static class User {
            private String firstName;
            private String lastName;
            private String email;

            public String getFirstName() {
                return firstName;
            }

            public String getLastName() {
                return lastName;
            }

            public String getEmail() {
                return email;
            }
        }
    }

    // Response model for fetching a user's details
    private static class UserResponse {
        private String firstName;
        private String lastName;
        private String email;

        @SerializedName("user_encryption_key")
        private String userEncryptionKey;

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }
        public String getUserEncryptionKey() {
            return userEncryptionKey;
        }
    }
}
