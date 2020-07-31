package com.mz.segiu.mvp.model;

import android.app.Application;

import com.google.gson.Gson;
import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.integration.IRepositoryManager;
import com.jess.arms.mvp.BaseModel;
import com.mz.segiu.api.service.CommonService;
import com.mz.segiu.mvp.contract.MzWebContract;
import com.mz.segiu.mvp.model.entity.UpFileEntity;

import java.io.File;

import javax.inject.Inject;

import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


@ActivityScope
public class MzWebModel extends BaseModel implements MzWebContract.Model{
    @Inject
    Gson mGson;
    @Inject
    Application mApplication;

    @Inject
    public MzWebModel(IRepositoryManager repositoryManager) {
        super(repositoryManager);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mGson = null;
        this.mApplication = null;
    }

    @Override
    public Observable<UpFileEntity> uploadFiles(File file) {

//        RetrofitUrlManager.getInstance().putDomain("upload", Api.BASE_UPLOAD_URL);
        //构建body
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("*/*"), file));
        RequestBody requestBody = builder.build();

        return mRepositoryManager.obtainRetrofitService(CommonService.class).uploadFile(requestBody);
    }
}