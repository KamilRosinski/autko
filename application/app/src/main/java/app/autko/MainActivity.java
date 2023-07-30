package app.autko;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.Arrays;

import app.autko.constant.BtMessageCodes;
import app.autko.databinding.ActivityMainBinding;
import app.autko.exception.BtException;
import app.autko.fragment.BtScanDialogFragment;
import app.autko.service.BtService;
import app.autko.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private BluetoothAdapter btAdapter;

    private TextView label;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    viewModel.setBtState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
                }
                case BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    viewModel.setConnected(true);
                }
                case BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    viewModel.setConnected(false);
                }
            }
        }
    };

    private final ActivityResultLauncher<String> btEnableRequestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (granted) {
            enableBt();
        } else {
            showPermissionRejectedAppSettingsDialog("Grant BT permission via app settings in order to enable Bluetooth adapter.");
        }
    });

    private final ActivityResultLauncher<String> btScanRequestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
        if (granted) {
            startBtDiscovery();
        } else {
            showPermissionRejectedAppSettingsDialog("Grant BT permission via app settings in order to start Bluetooth scan.");
        }
    });

    private final ActivityResultLauncher<Intent> btEnableLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (Activity.RESULT_OK == result.getResultCode()) {
            startBtDiscovery();
        }
    });

    private BtService btService;

    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(final Message message) {
            switch (message.what) {
                case BtMessageCodes.BYTES_RECEIVED -> {
                    final byte[] bytes = (byte[]) message.obj;
                    Log.d("BT MESSAGE HANDLER", "Bytes received: " + Arrays.toString(bytes) + ".");
                    final byte lastByte = bytes[bytes.length - 1];
                    label.setText(Integer.toString(Byte.toUnsignedInt(lastByte)));

                }
                case BtMessageCodes.EXCEPTION -> {
                    Log.d("BT MESSAGE HANDLER", "Exception!");
                    Toast.makeText(getApplicationContext(), ((BtException) message.obj).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);
        setContentView(binding.getRoot());

        btAdapter = getSystemService(BluetoothManager.class).getAdapter();
        viewModel.setBtSupported(btAdapter != null);
        viewModel.setBtState(btAdapter != null ? btAdapter.getState() : BluetoothAdapter.ERROR);

        final IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);

        registerReceiver(broadcastReceiver, intentFilter);

        btService = new BtService(btAdapter, handler);

        label = findViewById(R.id.label);

        final Button connectButton = findViewById(R.id.connectButton);
        connectButton.setOnClickListener(this::onConnectDisconnect);

        final SeekBar slider = findViewById(R.id.slider);
        viewModel.isConnected().observe(this, connected -> {
            connectButton.setEnabled(true);
            slider.setEnabled(connected);
            if (!connected && btService.isConnected()) {
                btService.disconnect();
            }
        });
        slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
                Log.d("MAIN ACTIVITY", "Send progress: " + progress + ".");
                btService.send(new byte[] {(byte) progress});
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    private void showPermissionRejectedAppSettingsDialog(final String message) {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Permission required")
                .setMessage(message)
                .setPositiveButton("Grant", (dialog, id) -> openAppSettings())
                .setNegativeButton("Deny", null)
                .create();
        alertDialog.show();
    }

    private void openAppSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
        } catch (final ActivityNotFoundException exception) {
            Toast.makeText(this, "Failed to open application settings.", Toast.LENGTH_SHORT).show();
        }
    }

    private void onConnectDisconnect(final View view) {
        view.setEnabled(false);
        if (Boolean.TRUE.equals(viewModel.isConnected().getValue())) {
            btService.disconnect();
        } else {
            if (!btAdapter.isEnabled()) {
                enableBt();
            } else {
                startBtDiscovery();
            }
        }

    }

    private void enableBt() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            btEnableLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } else {
            btEnableRequestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
    }

    private void startBtDiscovery() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            final boolean discoveryStarted = btAdapter.startDiscovery();
            if (discoveryStarted) {
                showBtDiscoveryDialog();
            } else {
                Toast.makeText(this, "Failed to start Bluetooth discovery.", Toast.LENGTH_SHORT).show();
            }
        } else {
          btScanRequestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_SCAN);
        }
    }

    private void showBtDiscoveryDialog() {
        new BtScanDialogFragment(btService::connect).show(getSupportFragmentManager(), BtScanDialogFragment.TAG);
    }

}
