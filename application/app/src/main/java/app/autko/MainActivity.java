package app.autko;

import android.Manifest;
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
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import app.autko.databinding.ActivityMainBinding;
import app.autko.viewmodel.MainActivityViewModel;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private MainActivityViewModel viewModel;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        viewModel.setProgress(0);

        final BluetoothManager btManager = getSystemService(BluetoothManager.class);
        final BluetoothAdapter btAdapter = btManager.getAdapter();
        viewModel.setBtSupported(btAdapter != null);
        viewModel.setBtState(btAdapter != null ? btAdapter.getState() : BluetoothAdapter.ERROR);

        final IntentFilter intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                viewModel.setBtState(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR));
            }
        };
        registerReceiver(receiver, intentFilter);

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) {
                enableBt();
            } else {
                showPermissionRejectedDialog();
            }
        });
    }

    private void showPermissionRejectedDialog() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Permission required")
                .setMessage("Grant BT permission via app settings in order to connect with Autko.")
                .setPositiveButton("Grant", (dialog, id) -> openAppSettings())
                .setNegativeButton("Deny", null)
                .create();
        alertDialog.show();
    }

    private void openAppSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null)));
        } catch (final ActivityNotFoundException exception) {
            final String errorMsg = "Failed to open application settings.";
            final Toast toastMessage = Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }

    public void onEnableBt(final View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            enableBt();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT);
        }
    }

    private void enableBt() {
        try {
            startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        } catch (final SecurityException exception) {
            final String errorMsg = String.format("Failed to enable Bluetooth: %s.", exception.getMessage());
            final Toast toastMessage = Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT);
            toastMessage.show();
        }
    }

}
