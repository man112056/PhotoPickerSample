package com.thecodeartist.photopickersample.fileupload;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.InputStream;

public class UriUtils {
    /**
     * Open InputStream from Uri
     */
    public static InputStream getInputStream(Context context, Uri uri) {
        try {
            return context.getContentResolver().openInputStream(uri);
        } catch (Exception e) {
            Log.e("Manish", "Failed to open InputStream: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get file name from Uri
     */
    public static String getFileName(Context context, Uri uri) {
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

        Log.d("Manish", "getFileName: " + result);
        return result;
    }

    /**
     * Get MIME type from Uri
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType = null;

        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            mimeType = context.getContentResolver().getType(uri);
        }

        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(extension.toLowerCase());
            }
        }

        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        Log.d("Manish", "getMimeType: " + mimeType);
        return mimeType;
    }
}
