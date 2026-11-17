package com.LuccasAlmeida.marketlists;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QrScannerActivity extends AppCompatActivity {

    public static final String EXTRA_QR_RESULT = "extra_qr_result";
    private PreviewView viewFinder;
    private ExecutorService cameraExecutor;
    private boolean qrDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        viewFinder = findViewById(R.id.viewFinder);
        ImageButton btnClose = findViewById(R.id.btnCloseScanner);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> finish());
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScanner scanner = BarcodeScanning.getClient();

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        Image mediaImage = imageProxy.getImage();
                        if (mediaImage != null && !qrDetected) {
                            InputImage image = InputImage.fromMediaImage(
                                    mediaImage,
                                    imageProxy.getImageInfo().getRotationDegrees()
                            );

                            scanner.process(image)
                                    .addOnSuccessListener(barcodes -> {
                                        for (Barcode barcode : barcodes) {
                                            String rawValue = barcode.getRawValue();
                                            if (rawValue != null && !qrDetected) {
                                                qrDetected = true;
                                                runOnUiThread(() -> onQrCodeScanned(rawValue));
                                                break;
                                            }
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        // Erro silencioso na analise de quadro
                                    })
                                    .addOnCompleteListener(task -> imageProxy.close());
                        } else {
                            imageProxy.close();
                        }
                    }
                });

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Erro ao iniciar câmera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void onQrCodeScanned(String qrUrl) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_QR_RESULT, qrUrl);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}