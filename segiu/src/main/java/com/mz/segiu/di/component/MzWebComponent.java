package com.mz.segiu.di.component;

import com.jess.arms.di.component.AppComponent;
import com.jess.arms.di.scope.ActivityScope;
import com.mz.segiu.di.module.MzWebModule;
import com.mz.segiu.mvp.contract.MzWebContract;
import com.mz.segiu.mvp.ui.activity.MzWebActivity;

import dagger.BindsInstance;
import dagger.Component;


@ActivityScope
@Component(modules = MzWebModule.class, dependencies = AppComponent.class)
public interface MzWebComponent {
    void inject(MzWebActivity activity);
    @Component.Builder
    interface Builder {
        @BindsInstance
        MzWebComponent.Builder view(MzWebContract.View view);
        MzWebComponent.Builder appComponent(AppComponent appComponent);
        MzWebComponent build();
    }
}