package com.mz.segiu.mvp.model;

import android.app.Application;

import com.google.gson.Gson;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;
import com.mz.segiu.api.service.EwoService;
import com.mz.segiu.api.service.UacService;
import com.mz.segiu.mvp.contract.ScanContract;
import com.mz.segiu.mvp.model.entity.EqDetailEntity;
import com.mz.segiu.mvp.model.entity.ProComboBoxEntity;

import javax.inject.Inject;

import io.reactivex.Observable;


@ActivityScope
public class ScanModel extends BaseModel implements ScanContract.Model{
    @Inject
    Gson mGson;
    @Inject
    Application mApplication;

    @Inject
    public ScanModel(IRepositoryManager repositoryManager) {
        super(repositoryManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mGson = null;
        this.mApplication = null;
    }

    @Override
    public Observable<ProComboBoxEntity> findProjectComboBoxList() {
        return mRepositoryManager.obtainRetrofitService(UacService.class).findProjectComboBoxList(2);
    }

    @Override
    public Observable<EqDetailEntity> getEquipmentDetails(String equipmentId, String equipmentNo) {
        return mRepositoryManager.obtainRetrofitService(EwoService.class).getEquipmentDetails(equipmentId,equipmentNo);
    }
}