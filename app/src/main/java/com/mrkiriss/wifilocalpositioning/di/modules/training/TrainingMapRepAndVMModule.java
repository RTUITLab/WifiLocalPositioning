package com.mrkiriss.wifilocalpositioning.di.modules.training;

import com.mrkiriss.wifilocalpositioning.data.sources.api.LocationDataApi;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingMapRepository;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingMapViewModel;

import dagger.Module;
import dagger.Provides;

@Module
public class TrainingMapRepAndVMModule {
    @Provides
    @TrainingScope
    public TrainingMapRepository provideRepository(LocationDataApi retrofit, MapImageManager mapImageManager, WifiScanner wifiScanner){
        return new TrainingMapRepository(retrofit, mapImageManager, wifiScanner);
    }

    @Provides
    @TrainingScope
    public TrainingMapViewModel provideTrainingMapViewModel(TrainingMapRepository repository){
        return new TrainingMapViewModel(repository);
    }
}
