package com.thecodeartist.photopickersample.fileupload;

import android.net.Uri;
import android.util.Log;

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
    private final InputStream inputStream;
    private String fileName;
    private String mimeType;

    public FileUploadHelper( InputStream inputStream, String fileName, String mimeType) {
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.mimeType = mimeType;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.escuelajs.co/api/v1/") //  test API
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(ApiService.class);
    }


    // Prepare file part
    private MultipartBody.Part prepareFilePart(String partName) {

        RequestBody requestBody = getRequestBody(inputStream, mimeType);
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
                    byte[] buffer = new byte[8 * 1024];
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




    //  Upload method
    public void uploadFileWithMeta(Uri fileUri) {
        // 1. Meta JSON
        MyModel model = new MyModel("101", "Test Title", "Some description");
        String json = new Gson().toJson(model);
        RequestBody metaPart = RequestBody.create(
                json, MediaType.parse("application/json")
        );
        Log.d("Manish", "metaPart: " + metaPart);

        // 2. File part
        MultipartBody.Part filePart = prepareFilePart("file");

        // 3. File name part
        RequestBody fileNamePart = RequestBody.create(
                fileName, MediaType.parse("text/plain")
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
