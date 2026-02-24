package dev.wdona.gestorinventarioqr.scanner;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.rscja.barcode.BarcodeDecoder;
import com.rscja.barcode.BarcodeFactory;

public class ScannerManager {

    private BarcodeDecoder barcodeDecoder;
    private ScanCallback callback;
    private boolean isInitialized = false;

    public interface ScanCallback {
        void onScanResult(String data);
        void onScanError(String error);
        void onInitialized(boolean success);
    }

    public void init(Context context, ScanCallback callback) {
        this.callback = callback;

        new Thread(() -> {
            try {
                barcodeDecoder = BarcodeFactory.getInstance().getBarcodeDecoder();
                boolean result = barcodeDecoder.open(context);
                isInitialized = result;

                if (result) {
                    barcodeDecoder.setDecodeCallback(entity -> {
                        if (entity.getResultCode() == BarcodeDecoder.DECODE_SUCCESS) {
                            String data = entity.getBarcodeData();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                if (callback != null) {
                                    callback.onScanResult(data);
                                }
                            });
                        }
                    });
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onInitialized(result);
                    }
                });

            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (callback != null) {
                        callback.onScanError(e.getMessage());
                        callback.onInitialized(false);
                    }
                });
            }
        }).start();
    }

    public void startScan() {
        if (isInitialized && barcodeDecoder != null) {
            barcodeDecoder.startScan();
        }
    }

    public void stopScan() {
        if (barcodeDecoder != null) {
            barcodeDecoder.stopScan();
        }
    }

    public void release() {
        if (barcodeDecoder != null) {
            barcodeDecoder.close();
        }
        isInitialized = false;
    }

    public boolean isInitialized() {
        return isInitialized;
    }
}