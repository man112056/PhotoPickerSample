package com.thecodeartist.photopickersample;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "photo_prefs";
    private static final String KEY_URI = "saved_uri";

    // Registers a photo picker activity launcher in single-select mode.
    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    if (doesUriExist(uri)) {
                        Log.d("PhotoPicker", "Selected URI: " + uri);
                        Toast.makeText(this, "Picked: " + uri, Toast.LENGTH_SHORT).show();

                        // Save URI in SharedPreferences
                        saveUri(uri.toString());

                        // Load image
                        loadImage(uri);
                    } else {
                        Toast.makeText(this, "Invalid image!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("PhotoPicker", "No media selected");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button classicBtn = findViewById(R.id.classicBtn);
        imageView = findViewById(R.id.imagview);

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

    private void saveUri(String uriString) {
        sharedPreferences.edit().putString(KEY_URI, uriString).apply();
    }

    private void clearUri() {
        sharedPreferences.edit().remove(KEY_URI).apply();
    }
}