package jp.s5r.linenotifyclient.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface LineNotifyService {

    @FormUrlEncoded
    @POST("/api/notify")
    Call<NotifyResponse> postNotify(
            @Header("Authorization") String authorization,
            @Field("message") String message);

    @Multipart
    @POST("/api/notify")
    Call<NotifyResponse> postNotifyWithImage(
            @Header("Authorization") String authorization,
            @Part("message") RequestBody message,
            @Part MultipartBody.Part imageFile);


    class NotifyResponse {

        int status;

        String message;

        public NotifyResponse(final int status, final String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }
    }
}
