package com.mrkiriss.wifilocalpositioning.di.modules;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.sources.db.AppDatabase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagersModule {

    @Provides
    @Singleton
    public WifiScanner provideWifiScanner(Context context, AppDatabase db){
        return new WifiScanner(context, (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE), db);
    }

    @Provides
    @Singleton
    public MapImageManager provide(Context context){
        return new MapImageManager(context);
    }
}
