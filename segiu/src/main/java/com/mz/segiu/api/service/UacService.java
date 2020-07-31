package com.mz.segiu.api.service;

import com.mz.segiu.api.Api;
import com.mz.segiu.mvp.model.entity.ProComboBoxEntity;
import io.reactivex.Observable;
import retrofit2.http.*;

/**
 * @desc 平台接口
 */
public interface UacService {

    @GET(Api.FIND_PROJECT_COMBO_BOX_LIST)
    Observable<ProComboBoxEntity> findProjectComboBoxList(@Query("orgType") int orgType);

}