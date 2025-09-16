package com.thecodeartist.photopickersample;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private MaterialButton filePickerButton;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "photo_prefs";
    private static final String KEY_URI = "saved_uri";

    private  int REQ_CODE_PDF = 102;

    // Registers a photo picker activity launcher in single-select mode.
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    if (doesUriExist(uri)) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        Toast.makeText(this, "Picked: " + uri, Toast.LENGTH_SHORT).show();

                        // Save URI in SharedPreferences with persistable permission
                        saveUri(uri);

                        // Load image
                        loadImage(uri);
                    } else {
                        Toast.makeText(this, "Invalid image!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button classicBtn = findViewById(R.id.classicBtn);
        imageView = findViewById(R.id.imagview);
        filePickerButton = findViewById(R.id.filePicker);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Check if we have a saved URI when activity loads
        String savedUri = sharedPreferences.getString(KEY_URI, null);
        if (savedUri != null) {
            Uri uri = Uri.parse(savedUri);
            if (doesUriExist(uri)) {
                loadImage(uri);
            } else {
                Toast.makeText(this, "Saved image not found", Toast.LENGTH_SHORT).show();
                clearUri();
            }
        }

        classicBtn.setOnClickListener(v -> openPhotoPicker());
        filePickerButton.setOnClickListener(v -> {
            openPdfPicker();

        });
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // important
        startActivityForResult(intent, REQ_CODE_PDF);

    }

    private void openPhotoPicker() {
        // Pick only images
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void loadImage(Uri uri) {
        Picasso.get()
                .load(uri)
                .placeholder(R.drawable.ic_loading)
                .into(imageView);
    }

    private boolean doesUriExist(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                inputStream.close();
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void saveUri(Uri uri) {
        // Take persistable read permission so it works after app restart
        final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
        } catch (SecurityException e) {
            Log.e("PhotoPicker", "Failed to take persistable permission: " + e.getMessage());
        }

        sharedPreferences.edit().putString(KEY_URI, uri.toString()).apply();
    }

    private void clearUri() {
        String savedUri = sharedPreferences.getString(KEY_URI, null);
        if (savedUri != null) {
            Uri uri = Uri.parse(savedUri);
            try {
                getContentResolver().releasePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException e) {
                Log.w("PhotoPicker", "No persistable permission to release: " + e.getMessage());
            }
        }
        sharedPreferences.edit().remove(KEY_URI).apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_PDF && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri pdfUri = data.getData();
                Log.d("MainActivity", "Selected PDF: " + pdfUri);
                Toast.makeText(this, "Picked PDF: " + pdfUri, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(this, PdfViewActivity.class);
                String uriInString = pdfUri.toString();
//                File file = new File(uriInString);
                intent.putExtra("pdf_uri", uriInString.toString());
                startActivity(intent);
            }
        }
    }
}
