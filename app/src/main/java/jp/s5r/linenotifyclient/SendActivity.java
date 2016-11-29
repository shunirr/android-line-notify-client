package jp.s5r.linenotifyclient;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import jp.s5r.linenotifyclient.api.LineNotifyService;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SendActivity extends Activity {

    private LineNotifyService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        Bundle extras = intent.getExtras();
        if (extras == null) {
            finish();
            return;
        }

        String text = extras.getString(Intent.EXTRA_TEXT, null);
        Uri imageUrl = extras.getParcelable(Intent.EXTRA_STREAM);
        if (imageUrl != null) {
            postNotifyWithImage(text, new ContentImage(getContentResolver(), imageUrl));
        } else if (text != null) {
            postNotify(text);
        } else {
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
        // Disable activity animation
        overridePendingTransition(0, 0);
    }

    private String getAuthorization() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String token = prefs.getString(getString(R.string.prefs_line_notify_token), "");
        return "Bearer " + token;
    }

    private String getBaseUrl() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString(getString(R.string.prefs_line_notify_server_url), getString(R.string.default_line_notify_server_url));
    }

    private LineNotifyService getService() {
        if (service == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getBaseUrl())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            service = retrofit.create(LineNotifyService.class);
        }

        return service;
    }

    private void postNotify(String message) {
        getService().postNotify(getAuthorization(), message).enqueue(new Callback<LineNotifyService.NotifyResponse>() {
            @Override
            public void onResponse(Call<LineNotifyService.NotifyResponse> call, Response<LineNotifyService.NotifyResponse> response) {
                SendActivity.this.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<LineNotifyService.NotifyResponse> call, Throwable t) {
                SendActivity.this.onFailure(call, t);
            }
        });
    }

    private void postNotifyWithImage(String message, ContentImage image) {
        getService().postNotifyWithImage(
                getAuthorization(),
                RequestBody.create(MediaType.parse("multipart/form-data"), (message == null) ? "" : message),
                image.getMultipartBody("imageFile", "image.jpg"))
                .enqueue(new Callback<LineNotifyService.NotifyResponse>() {
                    @Override
                    public void onResponse(Call<LineNotifyService.NotifyResponse> call, Response<LineNotifyService.NotifyResponse> response) {
                        SendActivity.this.onResponse(call, response);
                    }

                    @Override
                    public void onFailure(Call<LineNotifyService.NotifyResponse> call, Throwable t) {
                        SendActivity.this.onFailure(call, t);
                    }
                });
    }

    private void onResponse(Call<LineNotifyService.NotifyResponse> call,
                            Response<LineNotifyService.NotifyResponse> response) {
        if (response.isSuccessful()) {
            showSuccessToast();
        } else {
            showFailedToast();
        }
        finish();
    }

    private void onFailure(Call<LineNotifyService.NotifyResponse> call, Throwable t) {
        showFailedToast();
        finish();
    }

    private void showSuccessToast() {
        Toast.makeText(this, "Success notify", Toast.LENGTH_SHORT).show();
    }

    private void showFailedToast() {
        Toast.makeText(this, "Failed notify", Toast.LENGTH_SHORT).show();
    }
}
