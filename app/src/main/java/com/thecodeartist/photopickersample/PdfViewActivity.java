package com.thecodeartist.photopickersample;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;
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
                String fileName = getFileName(uri);
                String mimeType = getMimeType(uri);
                FileUploadHelper helper = new FileUploadHelper(inputStream, fileName, mimeType);
                helper.uploadFileWithMeta(Uri.parse(strUri));
            } catch (Exception e) {
                Log.d("Manish", "exception occured in content resolving: "+e.getMessage());
                Toast.makeText(this, "exception occured in content resolving", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get actual file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(nameIndex);
                }
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        Log.d("Manish", "getFileName: "+result);
        return result;
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;

        // 1. Directly from ContentResolver
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = getContentResolver().getType(uri);
        }

        // 2. If still null, try from file extension
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extension.toLowerCase());
            }
        }

        // 3. Last fallback (only if nothing found)
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        Log.d("Manish", "getMimeType: " + mimeType);
        return mimeType;
    }
}
