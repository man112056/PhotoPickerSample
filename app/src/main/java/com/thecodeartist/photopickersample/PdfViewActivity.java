package com.thecodeartist.photopickersample;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.thecodeartist.photopickersample.fileupload.FileUploadHelper;

import java.io.InputStream;

public class PdfViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pdf_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        String strUri = getIntent().getStringExtra("pdf_uri");
        if (strUri == null) {
            Log.d("Manish", "strUri is null in PdfActivity: ");
        } else {
            Uri uri = Uri.parse(strUri);
            Log.d("Manish", "strUri in PdfActivity: " + strUri);
            TextView textView = findViewById(R.id.textview);
            textView.setText("Selected PDF URI:\n" + strUri);

            // convert uri to input stream
            InputStream inputStream = null;

            try {
                inputStream = getContentResolver().openInputStream(uri);
                FileUploadHelper helper = new FileUploadHelper(this, inputStream);
                helper.uploadFileWithMeta(Uri.parse(strUri));
            } catch (Exception e) {
                Log.d("Manish", "exception occured in content resolving: "+e.getMessage());
                Toast.makeText(this, "exception occured in content resolving", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
