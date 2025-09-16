package com.thecodeartist.photopickersample.fileupload;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

class UriRequestBody extends RequestBody {

    private Context context;
    private Uri uri;
    private String contentType;

    public UriRequestBody(Context context, Uri uri, String contentType) {
        this.context = context;
        this.uri = uri;
        this.contentType = contentType;
    }

    @Nullable
    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream != null) {
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                sink.write(buffer, 0, read);
            }
            inputStream.close();
        }
    }
}

