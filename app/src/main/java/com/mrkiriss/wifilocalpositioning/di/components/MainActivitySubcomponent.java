package com.mrkiriss.wifilocalpositioning.di.components;

import com.mrkiriss.wifilocalpositioning.mvvm.view.MainActivity;

import dagger.Subcomponent;

@Subcomponent
public interface MainActivitySubcomponent {
    void inject(MainActivity mainActivity);

    @Subcomponent.Builder
    interface Builder{
        MainActivitySubcomponent build();
    }
}
