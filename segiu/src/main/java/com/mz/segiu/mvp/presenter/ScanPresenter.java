package com.mz.segiu.mvp.presenter;

import android.Manifest;
import android.app.Application;

import com.jess.arms.di.scope.ActivityScope;
import com.jess.arms.http.imageloader.ImageLoader;
import com.jess.arms.integration.AppManager;
import com.jess.arms.mvp.BasePresenter;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.PermissionUtil;
import com.jess.arms.utils.RxLifecycleUtils;
import com.mz.segiu.R;
import com.mz.segiu.mvp.contract.ScanContract;
import com.mz.segiu.mvp.model.entity.EqDetailEntity;
import com.mz.segiu.mvp.model.entity.ProComboBoxEntity;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import me.jessyan.rxerrorhandler.handler.RetryWithDelay;


@ActivityScope
public class ScanPresenter extends BasePresenter<ScanContract.Model, ScanContract.View> {
    @Inject
    RxErrorHandler mErrorHandler;
    @Inject
    Application mApplication;
    @Inject
    ImageLoader mImageLoader;
    @Inject
    AppManager mAppManager;
    @Inject
    RxPermissions mRxPermissions;

    @Inject
    public ScanPresenter(ScanContract.Model model, ScanContract.View rootView) {
        super(model, rootView);
    }

    public void initPremission() {
        //请求外部存储权限用于适配android6.0的权限管理机制
        PermissionUtil.requestPermission(
                new PermissionUtil.RequestPermission() {
                    @Override
                    public void onRequestPermissionSuccess() {
                        mRootView.requestPermissionSuccess();
                    }

                    @Override
                    public void onRequestPermissionFailure(List<String> permissions) {
                        mRootView.showMessage("申请权限失败");
                    }

                    @Override
                    public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                        mRootView.showMessage(mApplication.getString(R.string.go_to_setting_open_permission));
                    }

                },
                mRxPermissions, mErrorHandler, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA);
    }

    public void findProjectComboBoxList(String equipmentNo) {
        mModel.findProjectComboBoxList()
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 2)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                .doOnSubscribe(disposable -> mRootView.showLoading(""))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> mRootView.hideLoading())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView)) //使用 Rxlifecycle,使 Disposable 和 Activity 一起销毁
                .subscribe(new ErrorHandleSubscriber<ProComboBoxEntity>(mErrorHandler) {
                    @Override
                    public void onNext(ProComboBoxEntity data) {
                        List<ProComboBoxEntity> result = data.result;
                        if (result != null && result.size() > 0) {
                            getEquipmentDetails(equipmentNo, result);
                        }
                    }
                });
    }

    public void getEquipmentDetails(String equipmentNo, List<ProComboBoxEntity> result) {
        mModel.getEquipmentDetails("", equipmentNo)
                .subscribeOn(Schedulers.io())
                .retryWhen(new RetryWithDelay(3, 2)) //遇到错误时重试,第一个参数为重试几次,第二个参数为重试的间隔
                .doOnSubscribe(disposable -> mRootView.showLoading(""))
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> mRootView.hideLoading())
                .compose(RxLifecycleUtils.bindToLifecycle(mRootView)) //使用 Rxlifecycle,使 Disposable 和 Activity 一起销毁
                .subscribe(new ErrorHandleSubscriber<EqDetailEntity>(mErrorHandler) {
                    @Override
                    public void onNext(EqDetailEntity eqDetailEntity) {
                        checkEquipment(equipmentNo, eqDetailEntity, result);
                    }

                });
    }

    public void checkEquipment(String equipmentNo, EqDetailEntity eqDetailEntity, List<ProComboBoxEntity> result) {
        if (result != null && !result.isEmpty()) {
            for (ProComboBoxEntity data : result) {
                if (data.id == eqDetailEntity.institutionId) {
                    mRootView.resInputCode(equipmentNo, eqDetailEntity.institutionId);
                    return;
                }
            }
            mRootView.showMessage("您当前没有该设备的项目权限");
        } else {
            mRootView.showMessage("您当前没有该设备的项目权限");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mErrorHandler = null;
        this.mAppManager = null;
        this.mImageLoader = null;
        this.mApplication = null;
    }
}
