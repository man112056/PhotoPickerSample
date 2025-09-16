package com.thecodeartist.photopickersample;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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
            Log.d("Manish", "strUri in PdfActivity: " + strUri);
            TextView textView = findViewById(R.id.textview);
            textView.setText("Selected PDF URI:\n" + strUri);

            FileUploadHelper helper = new FileUploadHelper(this);
            helper.uploadFileWithMeta(Uri.parse(strUri));
        }
    }
}
