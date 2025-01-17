package com.mrkiriss.wifilocalpositioning.mvvm.viewmodel;

import android.util.Log;

import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.mrkiriss.wifilocalpositioning.data.models.server.CompleteKitsContainer;
import com.mrkiriss.wifilocalpositioning.data.models.server.ScanInformation;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.data.models.map.Floor;
import com.mrkiriss.wifilocalpositioning.data.models.map.MapPoint;
import com.mrkiriss.wifilocalpositioning.mvvm.repositiries.TrainingMapRepository;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper=false)
public class TrainingMapViewModel extends ViewModel {

    private final TrainingMapRepository repository;

    private ObservableInt selectedMod;
    private ObservableBoolean showMapPoints;
    private ObservableInt floorNumber; // изменяеться через стрелки
    private ObservableField<MapPoint> selectedMapPoint;
    private ObservableField<String> serverResponse;

    private ObservableField<String> inputY;
    private ObservableField<String> inputX;
    private ObservableField<String> inputCabId;
    private ObservableInt selectedRoomType; // 0=true кабинет, 1=false коридор
    private ObservableInt selectedFloorId; // изменяется при изменении spinner

    private ObservableInt remainingNumberOfScanKits;
    private ObservableField<String> inputNumberOfScanKits;

    private ObservableField<MapPoint> selectedToChangMapPoint;
    private ObservableField<String> contentOnActionsButtonChangesNeighbors;
    public final String MODE_SELECT_MAIN="Редактировать связи";
    public final String MODE_ADD_SECONDLY="Добавить связь";
    private List<MapPoint> currentChangeableConnections;

    private final LiveData<Floor> changeFloor;
    private final LiveData<String> serverResponseRequest;
    private final MutableLiveData<String> requestToUpdateFloor;

    private final MutableLiveData<String> toastContent;
    private final MutableLiveData<int[]> moveCamera;

    private final LiveData<List<ScanInformation>> requestToSetListOfScanInformation;
    private final LiveData<CompleteKitsContainer> completeKitsOfScansResult;
    private final LiveData<Integer> remainingNumberOfScanning;

    private final MutableLiveData<List<MapPoint>> serverConnectionsResponse;
    private final MutableLiveData<MapPoint> requestToAddSecondlyMapPointToRV;
    private final MutableLiveData<List<MapPoint>> requestToChangeSecondlyMapPointListInRV; // заполняется при ответе сервера или обнулении

    public TrainingMapViewModel(TrainingMapRepository repository){

        this.repository=repository;

        changeFloor= repository.getChangeFloor();
        requestToUpdateFloor=repository.getRequestToUpdateFloor();
        toastContent=repository.getToastContent();
        moveCamera=new MutableLiveData<>();
        serverResponseRequest=repository.getServerResponse();
        completeKitsOfScansResult=repository.getCompleteKitsOfScansResult();
        remainingNumberOfScanning=repository.getRemainingNumberOfScanning();
        requestToAddSecondlyMapPointToRV =new MutableLiveData<>();
        requestToChangeSecondlyMapPointListInRV =new MutableLiveData<>();
        serverConnectionsResponse=repository.getServerConnectionsResponse();
        requestToSetListOfScanInformation=repository.getRequestToSetListOfScanInformation();

        selectedMod=new ObservableInt(0);
        showMapPoints=new ObservableBoolean(false);
        floorNumber = new ObservableInt(2);
        selectedMapPoint=new ObservableField<>();
        serverResponse=new ObservableField<>("");

        inputX=new ObservableField<>("");
        inputY=new ObservableField<>("");
        inputCabId=new ObservableField<>("");
        selectedRoomType=new ObservableInt(0);
        selectedFloorId=new ObservableInt(2);

        remainingNumberOfScanKits = new ObservableInt(0);
        inputNumberOfScanKits=new ObservableField<>("");

        selectedToChangMapPoint=new ObservableField<>();
        contentOnActionsButtonChangesNeighbors=new ObservableField<>(MODE_SELECT_MAIN);
        currentChangeableConnections =new ArrayList<>();

        // обновление этажа после смени режима показа точек
        showMapPoints.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                updateFloor("Измеение режима отображения точек");
            }
        });
    }

    public void processShowSelectedMapPoint(boolean afterButton){
        switch (selectedMod.get()){
            case 0:
                processModePointInfo(afterButton);
                break;
            case 1:
            case 2:
                startFindNearPoint();
                break;
            case 3:
                startFindNearPoint();
                inputCabId.set(selectedMapPoint.get().getRoomName());
                break;
        }

    }
    private void processModePointInfo(boolean afterButton){
        String inputX=this.inputX.get(), inputY=this.inputY.get(), inputCabId=this.inputCabId.get();
        int intX, intY;
        if (inputX.isEmpty() || inputY.isEmpty() || !inputX.matches("\\d+") || !inputY.matches("\\d+")
                || (intX=Integer.parseInt(inputX))<=0 || (intY=Integer.parseInt(inputY))<=0){
            toastContent.setValue("Коодринаты некорректны");
            return;
        }
        if (afterButton) {
            floorNumber.set(selectedFloorId.get());
            try {
                moveCamera.setValue(new int[]{intX, intY,
                        changeFloor.getValue().getFloorSchema().getWidth(), changeFloor.getValue().getFloorSchema().getHeight()});
            }catch (Exception e){
                e.printStackTrace();
            }
        }else{
            selectedFloorId.set(floorNumber.get());
        }
        MapPoint mapPoint = new MapPoint(intX, intY, inputCabId, selectedRoomType.get()==0);
        //selectedMapPoint.set(mapPoint);
        startFloorChanging(mapPoint);
    }
    private void startFindNearPoint(){
        String inputX=this.inputX.get(), inputY=this.inputY.get();
        int intX, intY;
        if (inputX.isEmpty() || inputY.isEmpty() || !inputX.matches("\\d+") || !inputY.matches("\\d+")
                || (intX=Integer.parseInt(inputX))<=0 || (intY=Integer.parseInt(inputY))<=0){
            toastContent.setValue("Коодринаты некорректны");
            return;
        }
        selectedMapPoint.set(repository.findMapPointInCurrentData(intX, intY, floorNumber.get()));
    }

    // функции изменения номера этажа, вызывают менеджер выбора этажа в зависимости от факторов
    public void arrowInc(){
        if (floorNumber.get()<4){
            floorNumber.set(floorNumber.get()+1);
            startFloorChanging();
        }
    }
    public void arrowDec(){
        if (floorNumber.get()>0){
            floorNumber.set(floorNumber.get()-1);
            startFloorChanging();
        }
    }

    // запускает менеджер выбора конечного состояния этажа (вызов из-за стрелок)
    public void startFloorChanging(){
        Log.i("TrainingMapViewModel","startFloorChanging with showMapPoints="+showMapPoints.get());
        repository.changeFloor(floorNumber.get(), showMapPoints.get());
    }
    // изменяет этаж, добавляя к треб. рисунку точку mapPoint
    public void startFloorChanging(MapPoint mapPoint){
        Log.i("TrainingMapViewModel","startFloorChanging with selected MapPoint with showMapPoints="+showMapPoints.get());
        repository.changeFloor(floorNumber.get(), showMapPoints.get(), mapPoint);
    }

    // запрос на обновление списков всех точек на этажах
    public void startUpdatingMapPointLists(){
        repository.startDownloadingDataOnPointsOnAllFloors();
    }

    // функция создания запроса на обновление этажа
    public void updateFloor(String content){
        requestToUpdateFloor.setValue(content);
    }

    // POINT INFO MODE
    // отправка информации о точке на сервер
    public void postPointInformationToServer(){
        String inputX=this.inputX.get(), inputY=this.inputY.get(), inputCabId=this.inputCabId.get();
        int intX, intY, floorId = floorNumber.get();
        if (inputX.isEmpty() || inputY.isEmpty() || !inputX.matches("\\d+") || !inputY.matches("\\d+")
                || (intX=Integer.parseInt(inputX))<=0 || (intY=Integer.parseInt(inputY))<=0){
            toastContent.setValue("Коодринаты некорректны");
            return;
        }
        // сгладить координаты, чтобы они находились на одной прямой
        intX-=intX%5;
        intY-=intY%5;

        repository.postFromTrainingWithCoordinates(intX, intY, inputCabId, floorId, String.valueOf(selectedRoomType.get()==0));
    }

    // SCANNING MODE
    // запустить сканирование выбранной точки
    public void startScanning(){
        if (selectedMapPoint.get()==null || selectedMapPoint.get().getY()<=0 || selectedMapPoint.get().getX()<=0 || selectedMapPoint.get().getRoomName().isEmpty()){
            toastContent.setValue("Точка для сканирования не выбрана");
            return;
        }
        if (inputNumberOfScanKits.get().isEmpty() || !inputNumberOfScanKits.get().matches("\\d+") || Integer.parseInt(inputNumberOfScanKits.get())<=0){
            toastContent.setValue("Неккоректное количетсво сканирований");
            return;
        }
        repository.runScanInManager(Integer.parseInt(inputNumberOfScanKits.get()), selectedMapPoint.get().getRoomName());
    }
    // вызов обработки результатов сканирования
    public void processCompleteKitsOfScanResults(CompleteKitsContainer completeKitsContainer){
        repository.processCompleteKitsOfScanResults(completeKitsContainer);
    }
    // запрос на информацию о сканированиях для выбранной точки
    public void getScanInfoAboutLocation(){
        if (selectedMapPoint.get()==null){
            toastContent.setValue("Точка не выбрана");
            return;
        }
        repository.getScanningInformationAboutLocation(selectedMapPoint.get().getRoomName());
    }

    // CHANGING MODE
    // обработка нажатия на кнопку действия (начало редактирования или добавление точки)
    public void selectActionForChangingNeighbors(){
        switch (contentOnActionsButtonChangesNeighbors.get()){
            case MODE_SELECT_MAIN:
                processSelectMainMode(selectedMapPoint.get());
                break;
            case MODE_ADD_SECONDLY:
                processAddSecondlyMode(selectedMapPoint.get());
                break;
        }
    }
    private void processSelectMainMode(MapPoint selectedPoint){
        if (selectedPoint==null){
            toastContent.setValue("Точка для изменения не выбрана");
            return;
        }
        repository.startDownloadingConnections(selectedPoint.getRoomName());
    }
    public void processServerConnectionsResponse(List<MapPoint> mapPoints){
        currentChangeableConnections =new ArrayList<>(mapPoints);
        selectedToChangMapPoint.set(selectedMapPoint.get());
        contentOnActionsButtonChangesNeighbors.set(MODE_ADD_SECONDLY);
    }
    private void processAddSecondlyMode(MapPoint selectedPoint){
        if (selectedPoint==null){
            toastContent.setValue("Точка для добавления не выбрана");
            return;
        }
        for (MapPoint currentMapPoint:currentChangeableConnections){
            if (currentMapPoint.equals(selectedPoint)){
                toastContent.setValue("Точка уже добавлена");
                return;
            }
        }
        if (selectedPoint.equals(selectedToChangMapPoint.get())){
            toastContent.setValue("Выбранная точка редактируется");
            return;
        }

        currentChangeableConnections.add(selectedPoint);
        requestToAddSecondlyMapPointToRV.setValue(selectedPoint);
    }
    public void processDeleteSecondly(MapPoint mapPoint){
        currentChangeableConnections.remove(mapPoint);
    }
    // обработка нажатия на кнопку подтверждения изменений соседей точки
    public void acceptPointChangingNeighbors(){
        repository.postChangedConnections(currentChangeableConnections, selectedToChangMapPoint.get().getRoomName());
        cancelPointChangingNeighbors();
    }
    // обработка нажатия на кнопку отмены изменений соседей точки
    public void cancelPointChangingNeighbors(){
        selectedToChangMapPoint.set(null);
        currentChangeableConnections=new ArrayList<>();
        requestToChangeSecondlyMapPointListInRV.setValue(new ArrayList<>());
        contentOnActionsButtonChangesNeighbors.set(MODE_SELECT_MAIN);
    }

    // DELETE MODE
    public void startDeletingLPINfo(){
        if (inputCabId.get().isEmpty()){
            toastContent.setValue("Неккоректное название точки");
            return;
        }
        repository.deleteLocationPointInfoOnServer(inputCabId.get());
    }
    public void startDeletingLP(){
        if (inputCabId.get().isEmpty()){
            toastContent.setValue("Неккоректное название точки");
            return;
        }
        repository.deleteLocationPointOnServer(inputCabId.get());
    }
}
