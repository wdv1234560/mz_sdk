package com.mz.segiu.mvp.contract;

import android.app.Activity;

import com.jess.arms.mvp.IModel;
import com.jess.arms.mvp.IView;
import com.mz.segiu.mvp.model.entity.EqDetailEntity;
import com.mz.segiu.mvp.model.entity.ProComboBoxEntity;

import io.reactivex.Observable;


public interface ScanContract {
    //对于经常使用的关于UI的方法可以定义到IView中,如显示隐藏进度条,和显示文字消息
    interface View extends IView {
        Activity getMzActivity();

        void requestPermissionSuccess();

        void resInputCode(String equipmentNo, String institutionId);
    }

    //Model层定义接口,外部只需关心Model返回的数据,无需关心内部细节,即是否使用缓存
    interface Model extends IModel {
        Observable<ProComboBoxEntity> findProjectComboBoxList();
        Observable<EqDetailEntity> getEquipmentDetails( String equipmentId, String equipmentNo);
    }
}
