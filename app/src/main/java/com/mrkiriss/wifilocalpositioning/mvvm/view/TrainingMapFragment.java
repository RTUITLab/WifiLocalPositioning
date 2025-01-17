package com.mrkiriss.wifilocalpositioning.mvvm.view;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.mrkiriss.wifilocalpositioning.R;
import com.mrkiriss.wifilocalpositioning.adapters.ScanInfoRVAdapter;
import com.mrkiriss.wifilocalpositioning.databinding.FragmentTrainingMapBinding;
import com.mrkiriss.wifilocalpositioning.adapters.MapPointsRVAdapter;
import com.mrkiriss.wifilocalpositioning.di.App;
import com.mrkiriss.wifilocalpositioning.mvvm.viewmodel.TrainingMapViewModel;

import javax.inject.Inject;

public class TrainingMapFragment extends Fragment {

    @Inject
    protected TrainingMapViewModel viewModel;
    private FragmentTrainingMapBinding binding;
    private PhotoView photoView;
    private MapPointsRVAdapter mapPointsAdapter;
    private ScanInfoRVAdapter scanInfoAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        App.getInstance().getComponentManager().getTrainingMapSubcomponent().inject(this);

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_training_map, container, false);

        initScanInfoAdapter();
        initMapPointsAdapter();
        initPhotoView();
        initObservers();

        binding.setViewModel(viewModel);

        return binding.getRoot();
    }

    private void initScanInfoAdapter(){
        scanInfoAdapter=new ScanInfoRVAdapter();
        binding.scanInfoRecyclerView.setAdapter(scanInfoAdapter);
        binding.scanInfoRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    private void initMapPointsAdapter(){
        mapPointsAdapter=new MapPointsRVAdapter();
        binding.mapPointsRV.setAdapter(mapPointsAdapter);
        binding.mapPointsRV.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    private void initPhotoView(){
        photoView=binding.photoViewTraining;
        photoView.setMaximumScale(7);
        photoView.setMinimumScale(1);
        photoView.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // определить координаты
                float[] values = new float[9];
                photoView.getImageMatrix().getValues(values);
                int x = (int) ((e.getX() - values[2]) / values[0]);
                int y = (int) ((e.getY() - values[5]) / values[4]);
                // сгладить координаты, чтобы они находились на одной прямой
                x=x-x%5;
                y=y-y%5;
                // потребовать изменения изображения
                viewModel.getInputX().set(String.valueOf(x));
                viewModel.getInputY().set(String.valueOf(y));
                viewModel.processShowSelectedMapPoint(false);
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return true;
            }
        });
    }
    private void initObservers(){
        viewModel.getChangeFloor().observe(getViewLifecycleOwner(), floor -> {
            if (floor==null){
                photoView.setImageBitmap(null);
                return;
            }
            changeBitmap(floor.getFloorSchema());
        });
        viewModel.getRequestToUpdateFloor().observe(getViewLifecycleOwner(), content->viewModel.startFloorChanging());
        viewModel.getToastContent().observe(getViewLifecycleOwner(), this::showToast);
        viewModel.getMoveCamera().observe(getViewLifecycleOwner(), this::moveCamera);
        viewModel.getServerResponseRequest().observe(getViewLifecycleOwner(), s -> viewModel.getServerResponse().set(s));
        viewModel.getCompleteKitsOfScansResult().observe(getViewLifecycleOwner(), data->viewModel.processCompleteKitsOfScanResults(data));
        viewModel.getRemainingNumberOfScanning().observe(getViewLifecycleOwner(), integer -> viewModel.getRemainingNumberOfScanKits().set(integer));
        // обработка событий, связанных с RV для mapPoints при изменении связей
        viewModel.getServerConnectionsResponse().observe(getViewLifecycleOwner(), mapPoints -> {
            viewModel.processServerConnectionsResponse(mapPoints);
            mapPointsAdapter.setContent(mapPoints);
        });
        mapPointsAdapter.getRequestToDeleteMapPoint().observe(getViewLifecycleOwner(), mapPoint -> {
            mapPointsAdapter.deleteMapPoint(mapPoint);
            viewModel.processDeleteSecondly(mapPoint);
        });
        viewModel.getRequestToAddSecondlyMapPointToRV().observe(getViewLifecycleOwner(), mapPoint -> mapPointsAdapter.addMapPoint(mapPoint));
        viewModel.getRequestToChangeSecondlyMapPointListInRV().observe(getViewLifecycleOwner(), mapPoints -> mapPointsAdapter.setContent(mapPoints));
        // вставляем информацию о сканированиях точки в адаптер
        viewModel.getRequestToSetListOfScanInformation().observe(getViewLifecycleOwner(), content -> scanInfoAdapter.setContent(content));


        viewModel.startFloorChanging();
    }
    private void changeBitmap(Bitmap bitmap){
        Log.i("TrainingMapFragment","request to change bitmap" );
        Matrix matrix = new Matrix();
        photoView.getSuppMatrix(matrix);
        photoView.setImageBitmap(bitmap);
        photoView.setSuppMatrix(matrix);
    }
    private void showToast(String content){
        Toast.makeText(getContext(), content, Toast.LENGTH_SHORT).show();
    }
    private void moveCamera(int[] coordinates){
        photoView.setScale(4f, photoView.getRight()*(float)coordinates[0]/coordinates[2],
                photoView.getBottom()*(float)coordinates[1]/coordinates[3] , false);
    }
}