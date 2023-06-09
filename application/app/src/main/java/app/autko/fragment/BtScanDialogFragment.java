package app.autko.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import app.autko.BtDeviceArrayAdapter;
import app.autko.R;
import app.autko.databinding.DialogBtScanBinding;
import app.autko.viewmodel.BtScanDialogViewModel;

public class BtScanDialogFragment extends DialogFragment {

    public static final String TAG = "BtScanDialog";

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            switch (intent.getAction()) {
                case BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.d("BT SCAN", "Scan started");
                    viewModel.setScanning(true);
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BT SCAN", "Scan finished");
                    viewModel.setScanning(false);
                }
                case BluetoothDevice.ACTION_FOUND -> {
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                    Log.d("BT SCAN", "Device found: " + device.getAddress());
                    listAdapter.add(device);
                }
            }
        }
    };

    private BtScanDialogViewModel viewModel;

    private BluetoothAdapter btAdapter;

    private ListView lvDevices;

    private BtDeviceArrayAdapter listAdapter;

    @Override
    public void onCreate(final Bundle bundle) {
        Log.d("LIFECYCLE", "onCreate()");
        super.onCreate(bundle);
        viewModel = new ViewModelProvider(this).get(BtScanDialogViewModel.class);
        btAdapter = getActivity().getSystemService(BluetoothManager.class).getAdapter();

        viewModel.isScanning().observe(this, isScanning -> {
            if (isScanning) {
                onScanningStarted();
            }
        });


        listAdapter = new BtDeviceArrayAdapter(getContext());

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        viewModel.setScanning(btAdapter.isDiscovering());
    }

    private void onScanningStarted() {
        getPositiveButton().setEnabled(false);
        lvDevices.clearChoices();
        listAdapter.clear();
        if (lvDevices.getOnItemClickListener() == null) {
            lvDevices.setOnItemClickListener((parent, view, position, id) -> {
                getPositiveButton().setEnabled(true);
                lvDevices.setOnItemClickListener(null);
            });
        }
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        Log.d("LIFECYCLE", "onCreateDialog()");

        final DialogBtScanBinding binding = DialogBtScanBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        final Button btnScan = binding.getRoot().findViewById(R.id.btnScan);
        btnScan.setOnClickListener(view -> btAdapter.startDiscovery());

        lvDevices = binding.getRoot().findViewById(R.id.lvDevices);
        lvDevices.setAdapter(listAdapter);

        final AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Connect to robot")
                .setView(binding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Connect", (dialog, id) -> {
                    Log.d("CONNECT", "Connect to: " + lvDevices.getAdapter().getItem(lvDevices.getCheckedItemPosition()) + ".");
                })
                .create();

        alertDialog.setOnShowListener(dialog -> ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false));


        return alertDialog;
    }

    private Button getPositiveButton() {
        return ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
    }

    @Override
    public void onDestroy() {
        Log.d("LIFECYCLE", "onDestroy()");
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
        btAdapter.cancelDiscovery();
    }

}
