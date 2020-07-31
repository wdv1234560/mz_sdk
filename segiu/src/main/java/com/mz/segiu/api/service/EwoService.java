package com.mz.segiu.api.service;

import com.mz.segiu.api.Api;
import com.mz.segiu.mvp.model.entity.EqDetailEntity;
import io.reactivex.Observable;
import org.jetbrains.annotations.NotNull;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * @desc 设备管理接口
 */
public interface EwoService {
    @FormUrlEncoded
    @POST(Api.GET_EQUIPMENT_DETAILS)
    Observable<EqDetailEntity> getEquipmentDetails(@Field("equipmentId") @NotNull String equipmentId, @Field("equipmentNo") @NotNull String equipmentNo);
}