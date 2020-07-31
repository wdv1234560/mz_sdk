package com.mz.segiu.di.component;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.scope.ActivityScope;
import com.mz.segiu.di.module.ScanModule;
import com.mz.segiu.mvp.contract.ScanContract;
import com.mz.segiu.mvp.ui.activity.ScanActivity;

import dagger.BindsInstance;
import dagger.Component;


@ActivityScope
@Component(modules = ScanModule.class, dependencies = AppComponent.class)
public interface ScanComponent {
    void inject(ScanActivity activity);
    @Component.Builder
    interface Builder {
        @BindsInstance
        ScanComponent.Builder view(ScanContract.View view);
        ScanComponent.Builder appComponent(AppComponent appComponent);
        ScanComponent build();
    }
}