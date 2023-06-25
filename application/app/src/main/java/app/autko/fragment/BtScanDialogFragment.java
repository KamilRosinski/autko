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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

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
                    listAdapter.clear();
                    viewModel.setScanning(true);
                }
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d("BT SCAN", "Scan finished");
                    viewModel.setScanning(false);
                }
                case BluetoothDevice.ACTION_FOUND -> {
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);
                    Log.d("BT SCAN", "Device found: " + device.getAddress());
                    listAdapter.add(String.format("%s (%s)", device.getName(), device.getAddress()));
                }
            }
        }
    };

    private BtScanDialogViewModel viewModel;

    private BluetoothAdapter btAdapter;

    private DialogBtScanBinding binding;

    private ListView lvDevices;

    private ArrayAdapter<String> listAdapter;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(BtScanDialogViewModel.class);

        binding = DialogBtScanBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(this);
        binding.setViewModel(viewModel);

        return new AlertDialog.Builder(requireContext())
                .setTitle("Connect to robot")
                .setView(binding.getRoot())
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Connect", (dialog, id) -> {
                    Log.d("CONNECT", "Connect to: " + lvDevices.getAdapter().getItem(lvDevices.getCheckedItemPosition()) + ".");
                })
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        btAdapter = getActivity().getSystemService(BluetoothManager.class).getAdapter();
        viewModel.setScanning(btAdapter.isDiscovering());

        final Button btnScan = binding.getRoot().findViewById(R.id.btnScan);
        btnScan.setOnClickListener(this::onScan);

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        getActivity().registerReceiver(broadcastReceiver, intentFilter);

        final Button positiveButton = ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false);

        listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice);

        lvDevices = binding.getRoot().findViewById(R.id.lvDevices);
        lvDevices.setAdapter(listAdapter);
        lvDevices.setOnItemClickListener((parent, view, position, id) -> {
            if (!positiveButton.isEnabled()) {
                positiveButton.setEnabled(true);
            }
        });
    }

    private void onScan(final View view) {
        final boolean started = btAdapter.startDiscovery();
        Log.d("BT", "Started? " + started);
    }
}
