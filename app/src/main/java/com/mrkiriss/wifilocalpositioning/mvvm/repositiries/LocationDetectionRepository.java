package com.mrkiriss.wifilocalpositioning.mvvm.repositiries;

import android.net.wifi.ScanResult;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.Connections;
import com.mrkiriss.wifilocalpositioning.data.models.server.LocationPointInfo;
import com.mrkiriss.wifilocalpositioning.data.sources.MapImageManager;
import com.mrkiriss.wifilocalpositioning.data.sources.WifiScanner;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.FloorId;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.AccessPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.CalibrationLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.models.server.DefinedLocationPoint;
import com.mrkiriss.wifilocalpositioning.data.sources.IMWifiServerApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@Data
public class LocationDetectionRepository implements Serializable {

    private final IMWifiServerApi retrofit;
    private final WifiScanner wifiScanner;
    private final MapImageManager mapImageManager;

    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final MutableLiveData<MapPoint> resultOfDefinition;
    private final MutableLiveData<Floor> changeFloor;
    private final MutableLiveData<String> toastContent;

    public LocationDetectionRepository(IMWifiServerApi retrofit, WifiScanner wifiScanner, MapImageManager mapImageManager){
        this.retrofit=retrofit;
        this.wifiScanner=wifiScanner;
        this.mapImageManager = mapImageManager;

        completeKitsOfScansResult=wifiScanner.getCompleteScanResults();

        resultOfDefinition=new MutableLiveData<>();
        changeFloor=new MutableLiveData<>();
        toastContent=new MutableLiveData<>();

        wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
    }

    // floor
    public void changeFloor(int floorIdInt, boolean showRoute){
        if (showRoute){
            changeFloor.setValue(mapImageManager.getRouteFloor(Floor.convertFloorIdToEnum(floorIdInt)));
        }else{
            Floor foundFloor=mapImageManager.getBasicFloor(Floor.convertFloorIdToEnum(floorIdInt));
            changeFloor.setValue(foundFloor);
        }
    }

    private Floor downloadSingleFloorForPointer(FloorId floorId, int x, int y){
        Floor result = mapImageManager.getFloorWithPointer(floorId, x, y);
        return result;
    }

    // scanning
    public void startProcessingCompleteKitsOfScansResult(CompleteKitsContainer completeKitsContainer){

        if (completeKitsContainer.getRequestSourceType()!=WifiScanner.TYPE_DEFINITION) return;

        CalibrationLocationPoint calibrationLocationPoint = new CalibrationLocationPoint();
        for (List<ScanResult> oneScanResults: completeKitsContainer.getCompleteKits()) {
            List<AccessPoint> accessPoints = new ArrayList<>();
            for (ScanResult scanResult : oneScanResults) {
                accessPoints.add(new AccessPoint(scanResult.BSSID, scanResult.level));
            }
            // создаём запрос на добавление информации набора на экран пользователя
            calibrationLocationPoint.addOneCalibrationSet(accessPoints);
        }

        // отправляем запрос на сервер
        //postFromDefinitionWithCabinet(calibrationLocationPoint);
    }

    // server
    private void postFromDefinitionWithCabinet(CalibrationLocationPoint calibrationLocationPoint){

        if (calibrationLocationPoint.getCalibrationSets()==null || calibrationLocationPoint.getCalibrationSets().size()==0){
            wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
            return;
        }

        retrofit.defineLocation(calibrationLocationPoint).enqueue(new Callback<DefinedLocationPoint>() {
            @Override
            public void onResponse(Call<DefinedLocationPoint> call, Response<DefinedLocationPoint> response) {
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);

                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server definition=%s", response.body()));

                if (response.body()==null || response.body().getFloorId()==-1)return;

                Log.println(Log.INFO, "SEND_CONVERT_RESULT",
                        convertToMapPoint(response.body()).toString());

                resultOfDefinition.setValue(convertToMapPoint(response.body()));
            }
            @Override
            public void onFailure(Call<DefinedLocationPoint> call, Throwable t) {
                Log.e("SERVER_ERROR", t.getMessage());
                wifiScanner.startDefiningScan(WifiScanner.TYPE_DEFINITION);
            }
        });
    }
    private MapPoint convertToMapPoint(DefinedLocationPoint definedLocationPoint){
        MapPoint result = new MapPoint();

        Floor foundFloor=downloadSingleFloorForPointer(Floor.convertFloorIdToEnum(definedLocationPoint.getFloorId()),
                definedLocationPoint.getX(), definedLocationPoint.getY());

        result.setFloorWithPointer(foundFloor);
        result.setX(definedLocationPoint.getX());
        result.setY(definedLocationPoint.getY());
        result.setRoomName(definedLocationPoint.getRoomName());
        return result;
    }

    public void requestRoute(String start, String end){
        retrofit.getRoute(start, end).enqueue(new Callback<List<LocationPointInfo>>() {
            @Override
            public void onResponse(Call<List<LocationPointInfo>> call, Response<List<LocationPointInfo>> response) {
                Log.println(Log.INFO, "GOOD_DEFINITION_ROOM",
                        String.format("Server route=%s", response.body()));

                if (response.body()==null){
                    Log.e("SERVER_ERROR", "Response body is null");
                    toastContent.setValue("Маршрут построить не удалось");
                    return;
                }

                mapImageManager.startCreatingFloorsWithRout(response.body());
            }

            @Override
            public void onFailure(Call<List<LocationPointInfo>> call, Throwable t) {
                Log.e("SERVER_ERROR", t.getMessage());
                toastContent.setValue("Маршрут построить не удалось");
            }
        });
    }
}
