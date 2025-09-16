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
import com.thecodeartist.photopickersample.fileupload.UriUtils;

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

            // convert uri to input stream
            try {
                InputStream inputStream = UriUtils.getInputStream(this, uri);
                // Get filename & MIME type
                String fileName = UriUtils.getFileName(this, uri);
                String mimeType = UriUtils.getMimeType(this, uri);
                long fileSize = UriUtils.getFileSizeInKB(this, uri);
                Log.d("Manish", "fileSize: "+fileSize);
                FileUploadHelper helper = new FileUploadHelper(inputStream, fileName, mimeType);
                 helper.uploadFileWithMeta();

                 //TODO - as of now just displaying the details - no matter if upload is success or failure
                textView.setText("Selected PDF URI:---\n" + strUri +
                        "\n\nFile Name:--- " + fileName +
                        "\nMIME Type: --- " + mimeType +
                        "\nFile Size: ---" + fileSize + " KB");
            } catch (Exception e) {
                Log.d("Manish", "exception occured in content resolving: "+e.getMessage());
                Toast.makeText(this, "exception occured in content resolving", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
