package com.freeportmetrics.nearby.beaconsimulator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.freeportmetrics.nearby.beaconsimulator.databinding.ActivityMainBinding;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.utils.UrlBeaconUrlCompressor;

import java.net.MalformedURLException;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    //region Lifecycle
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setVm(nearbyViewModel);
        binding.setAh(nearbyActionHandler);
    }

    @Override
    protected void onStop() {
        stopAdvertising();
        super.onStop();
    }

    //endregion Lifecycle

    //region Nearby
    private static final int ADVERTISED_MANUFACTURER_ID = 0x0118;

    private static final int ADVERTISED_TRANSMIT_POWER = -59;

    private static final String ADVERTISED_URL_FM = "https://freeportmetrics.com";

    private final BeaconParser beaconParser = new BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_URL_LAYOUT);

    @Nullable
    private BeaconTransmitter beaconTransmitter;

    @Nullable
    private BeaconManager beaconManager;

    private void startAdvertising() {
        if (beaconManager == null) {
            beaconManager = BeaconManager.getInstanceForApplication(this);
        }
        if (!isBluetoothOn()) {
            Toast.makeText(this, R.string.enable_bluetooth, Toast.LENGTH_LONG).show();
            return;
        }
        if (!beaconManager.checkAvailability()) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_LONG).show();
            return;
        }
        if (beaconTransmitter == null) {
            beaconTransmitter = new BeaconTransmitter(getApplicationContext(), beaconParser);
        }
        try {
            Beacon beacon = new Beacon.Builder()
                    .setIdentifiers(Collections.singletonList(urlToIdentifier(nearbyViewModel.getUrl())))
                    .setManufacturer(ADVERTISED_MANUFACTURER_ID)
                    .setTxPower(ADVERTISED_TRANSMIT_POWER)
                    .build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        Log.d(LOG_TAG, "Advertising started.");
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        Log.w(LOG_TAG, "Advertising failure.");
                    }
                });
            } else {
                beaconTransmitter.startAdvertising(beacon);
            }
            nearbyViewModel.setAdvertising(true);
        } catch (final MalformedURLException ex) {
            Log.e(LOG_TAG, ex.getMessage());
        }
    }

    private void stopAdvertising() {
        if (beaconTransmitter != null) {
            beaconTransmitter.stopAdvertising();
            nearbyViewModel.setAdvertising(false);
        }
    }

    private Identifier urlToIdentifier(@NonNull String url) throws MalformedURLException {
        byte[] urlBytes = UrlBeaconUrlCompressor.compress(url);
        return Identifier.fromBytes(urlBytes, 0, urlBytes.length, false);
    }

    private boolean isBluetoothOn() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }
    //endregion Nearby

    //region Databinding
    private final NearbyViewModel nearbyViewModel = new NearbyViewModel(ADVERTISED_URL_FM);
    private final NearbyActionHandler nearbyActionHandler = new NearbyActionHandler() {
        @Override
        public void advertiseButtonClicked() {
            if (nearbyViewModel.isAdvertising()) {
                MainActivity.this.stopAdvertising();
            } else {
                MainActivity.this.startAdvertising();
            }
        }
    };
    //endregion Databinding
}
