package com.mz.segiu.api.service;

import io.reactivex.Observable;
import com.mz.segiu.api.Api;
import com.mz.segiu.mvp.model.entity.YFAuthorizeEntity;
import com.mz.segiu.mvp.model.entity.YFInventoryEntity;
import com.mz.segiu.mvp.model.entity.YFLDConfigEntity;
import com.mz.segiu.mvp.model.entity.YFProgressConfigEntity;
import org.jetbrains.annotations.NotNull;
import retrofit2.http.*;

public interface MwsService {

    @GET(Api.VERIFY_YF_CONFIG)
    Observable<YFLDConfigEntity> verifyAndGetConfig(@Query("organizationId") @NotNull String orgId);

    @GET(Api.GET_PROCESS_CONFIG+"/{organizationId}")
    @NotNull
    Observable<YFProgressConfigEntity> getProcessConfig(@Path("organizationId") @NotNull String orgId);

    @GET(Api.GET_AUTHORIZE_USER)
    @NotNull
    Observable<YFAuthorizeEntity> getAuthorizeUser(@Query("organizationId") @NotNull String orgId);

    @FormUrlEncoded
    @POST(Api.JUDGE_MAKE_INVENTORY_FLAG)
    Observable<YFInventoryEntity> judgeMakeInventoryFlag(@Field("organizationId") @NotNull String orgId);
}