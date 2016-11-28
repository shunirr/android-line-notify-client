package jp.s5r.linenotifyclient;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ContentImage {

    private ContentResolver contentResolver;

    private Uri contentUri;

    private WeakReference<Bitmap> bitmapRef;

    public ContentImage(ContentResolver contentResolver, Uri contentUri) {
        this.contentResolver = contentResolver;
        this.contentUri = contentUri;
    }

    private Bitmap getBitmap() throws IOException {
        if (bitmapRef != null) {
            Bitmap bitmap = bitmapRef.get();
            if (bitmap != null) {
                return bitmap;
            }
        }

        Bitmap image = MediaStore.Images.Media.getBitmap(contentResolver, contentUri);
        bitmapRef = new WeakReference<>(image);

        return image;
    }

    private byte[] getJpegByteArray() throws IOException {
        Bitmap image = getBitmap();

        byte[] imageByteArray = null;
        ByteArrayOutputStream bos = null;
        try {
            bos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 95, bos);
            imageByteArray = bos.toByteArray();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException ignored) {
                }
            }
        }

        return imageByteArray;
    }

    public MultipartBody.Part getMultipartBody(String name, String filename) {
        byte[] jpegByteArray = null;
        try {
            jpegByteArray = getJpegByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (jpegByteArray == null) {
            return null;
        }
        return MultipartBody.Part.createFormData(
                name,
                filename,
                RequestBody.create(MediaType.parse("image/*"), jpegByteArray));
    }
}
