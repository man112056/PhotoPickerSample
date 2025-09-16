package com.thecodeartist.photopickersample.fileupload;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FileUploadHelper {

    private final ApiService apiService;
    private final Context context;
    private final InputStream inputStream;

    public FileUploadHelper(Context context, InputStream inputStream) {
        this.context = context;
        this.inputStream = inputStream;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.escuelajs.co/api/v1/") //  test API
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }

    // Get actual file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
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

    // Prepare file part
    private MultipartBody.Part prepareFilePart(Uri uri, String partName) {
        String mimeType = getMimeType(uri);

    RequestBody requestBody = getRequestBody(inputStream, mimeType);

        String fileName = getFileName(uri);
        if (fileName == null) {
            fileName = "file_" + System.currentTimeMillis();
        }

        return MultipartBody.Part.createFormData(partName, fileName, requestBody);
    }

    private RequestBody getRequestBody(InputStream inputStream, String mimeType) {
        RequestBody requestBody = new RequestBody() {
            @Nullable
            @Override
            public MediaType contentType() {
                return MediaType.parse(mimeType);
            }

            @Override
            public void writeTo(@NonNull BufferedSink bufferedSink) throws IOException {
                if (inputStream != null) {
                    byte[] buffer = new byte[8*1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        bufferedSink.write(buffer, 0, read);
                    }
                    inputStream.close();
                }
            }
        };
        return requestBody;
    }

    private String getMimeType(Uri uri) {
        String mimeType = null;

        // 1. Directly from ContentResolver
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
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
        Log.d("Manish", "getMimeType: "+mimeType);
        return mimeType;
    }


    //  Upload method
    public void uploadFileWithMeta(Uri fileUri) {
        // 1. Meta JSON
        MyModel model = new MyModel("101", "Test Title", "Some description");
        String json = new Gson().toJson(model);
        RequestBody metaPart = RequestBody.create(
                json, MediaType.parse("application/json")
        );
        Log.d("Manish", "metaPart: "+metaPart);

        // 2. File part
        MultipartBody.Part filePart = prepareFilePart(fileUri, "file");

        // 3. File name part
        String actualFileName = getFileName(fileUri);
        RequestBody fileNamePart = RequestBody.create(
                actualFileName, MediaType.parse("text/plain")
        );

        // 4. API call
        Call<UploadResponse> call = apiService.uploadData(metaPart, filePart, fileNamePart);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                if (response.isSuccessful()) {
                    UploadResponse res = response.body();
                    Log.d("Manish", "Success: " + res.location);
                } else {
                    Log.e("Manish", "Error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Log.e("Manish", "Failed: " + t.getMessage());
            }
        });
    }
}
