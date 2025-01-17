package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScanRepAndVMModule;
import com.mrkiriss.wifilocalpositioning.di.modules.training.TrainingScope;
import com.mrkiriss.wifilocalpositioning.mvvm.view.TrainingScanFragment;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingScanViewModel;

import dagger.Subcomponent;

@Subcomponent(modules = {TrainingScanRepAndVMModule.class})
@TrainingScope
public interface TrainingScanSubcomponent {

    @Subcomponent.Builder
    interface Builder{
        TrainingScanSubcomponent.Builder repModule(TrainingScanRepAndVMModule repositoryModule);
        TrainingScanSubcomponent build();
    }

    void inject(TrainingScanFragment fragment);
}
