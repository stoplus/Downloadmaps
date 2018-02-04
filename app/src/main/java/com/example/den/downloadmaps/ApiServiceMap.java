package com.example.den.downloadmaps;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by den on 26.01.2018.
 */

public interface ApiServiceMap {

    @GET
    @Streaming
    Call<ResponseBody> downloadFileWithDynamicUrlSync(@Url String fileUrl);
}
