package com.nurtaz.dev.qrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecuter;
    private PreviewView previewView;
    private MyImageAnalyzer analyzer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.preview_view);

        this.getWindow().setFlags(1024,1024);
        //background job
        cameraExecuter = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        analyzer = new MyImageAnalyzer(getSupportFragmentManager());

        //camera Provider Future
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                //in background
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.CAMERA) != (PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.CAMERA},101);
                    }else {
                        ProcessCameraProvider processCameraProvider =
                                (ProcessCameraProvider) cameraProviderFuture.get();
                        bindPreview(processCameraProvider);
                    }
                }catch (ExecutionException e){
                    e.printStackTrace();
                } catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //requesting permission from user

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0){
            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();

            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bindPreview(processCameraProvider);
        }
    }

    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(
                CameraSelector.LENS_FACING_BACK
        ).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageCapture imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280,720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecuter,analyzer);
        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(this,cameraSelector,preview,
                imageCapture,imageAnalysis);

    }

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        private FragmentManager fragmentManager;
        private ButtonDialog buttonDialog;

        public MyImageAnalyzer(FragmentManager supportFragmentManager) {
            this.fragmentManager = supportFragmentManager;
            this.buttonDialog = new ButtonDialog();
        }

        @Override
        public void analyze(@NonNull ImageProxy image) {
            ScanBarCode(image);
        }


        private void ScanBarCode(ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image image = imageProxy.getImage();
            assert image != null;
            InputImage inputImage = InputImage.fromMediaImage(image,
                    imageProxy.getImageInfo().getRotationDegrees());

            BarcodeScannerOptions barcodeScannerOptions =
                    new BarcodeScannerOptions.Builder().setBarcodeFormats(
                            Barcode.FORMAT_QR_CODE,
                            Barcode.FORMAT_AZTEC
                    ).build();

            BarcodeScanner scanner = BarcodeScanning.getClient(barcodeScannerOptions);
            Task<List<Barcode>> result = scanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            ReaderBarCodeData(barcodes);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            //failure to read QR code
                            Toast.makeText(MainActivity.this,
                                    "Failed to read Code", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                        @Override
                        public void onComplete(@NonNull Task<List<Barcode>> task) {
                            image.close();
                        }
                    });
        }

        private void ReaderBarCodeData(List<Barcode> barcodes) {
            for (Barcode barcode : barcodes) {
                Rect bounds = barcode.getBoundingBox();
                Point[] corners = barcode.getCornerPoints();

                String raw = barcode.getRawValue();
                int valueType = barcode.getValueType();

                switch (valueType) {
                    case Barcode.TYPE_WIFI:
                        String ssid = barcode.getWifi().getSsid();
                        String password = barcode.getWifi().getPassword();
                        int type = barcode.getWifi().getEncryptionType();

                        break;
                    case Barcode.TYPE_URL:
                        if (!buttonDialog.isAdded()){
                            buttonDialog.show(fragmentManager,"");

                        }
                        buttonDialog.fetchURL(barcode.getUrl().getUrl());
                        String title = barcode.getUrl().getTitle();

                        String url = barcode.getUrl().getUrl();
                        break;

                }
            }
        }
    }
}