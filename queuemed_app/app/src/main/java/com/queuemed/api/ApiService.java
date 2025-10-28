package com.queuemed.api;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @POST("register.php")
    Call<Map<String, Object>> register(@Body Map<String, String> user);

    @POST("login.php")
    Call<Map<String, Object>> login(@Body Map<String, String> user);

    // for fetching user profile
    @GET("user/profile")
    Call<Map<String, Object>> getUserProfile(@Query("email") String email);

    // Get Appointments api method
    @POST("getAppointments.php")
    Call<Map<String, Object>> getAppointments(@Body Map<String, String> body);

    @POST("save_medical_history.php")
    Call<Map<String, Object>> saveMedicalHistory(@Body Map<String, Object> medicalHistory);

    @GET("get_medical_histories.php")
    Call<Map<String, Object>> getMedicalHistories(@Query("staff_id") int staffId);
    @GET("get_medical_history.php")
    Call<Map<String, Object>> getMedicalHistory(@Query("history_id") int historyId);

    @POST("delete_medical_history.php")
    Call<Map<String, Object>> deleteMedicalHistory(@Body Map<String, Object> requestBody);

    @POST("update_medical_history.php")
    Call<Map<String, Object>> updateMedicalHistory(@Body Map<String, Object> medicalHistory);
}
