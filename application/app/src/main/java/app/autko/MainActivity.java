package app.autko;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

import app.autko.databinding.ActivityMainBinding;
import app.autko.fragment.BtScanDialogFragment;
import app.autko.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;
    private BluetoothAdapter btAdapter;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction())) {
                viewModel.setBtState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
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
        if (result.getResultCode() == Activity.RESULT_OK) {
            startBtDiscovery();
        }
    });

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

        registerReceiver(broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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

    public void onConnect(final View view) {
        if (!btAdapter.isEnabled()) {
            enableBt();
        } else {
            startBtDiscovery();
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
        new BtScanDialogFragment().show(getSupportFragmentManager(), BtScanDialogFragment.TAG);
    }

}
