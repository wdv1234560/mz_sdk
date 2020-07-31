package com.mz.segiu.api.service;

import com.mz.segiu.mvp.model.entity.UpFileEntity;

import io.reactivex.Observable;
import com.mz.segiu.api.Api;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * ================================================
 * 存放通用的一些 API
 * ================================================
 */
public interface CommonService {
    @Headers("Domain-Name: upload") // Add the Domain-Name header
    @POST(Api.UPLOAD_FILE)
    Observable<UpFileEntity> uploadFile(@Body RequestBody requestBody);
}
