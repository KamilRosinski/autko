package app.autko.service;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

import app.autko.constant.BtMessageCodes;
import app.autko.exception.BtException;
import app.autko.thread.BtConnectThread;
import app.autko.thread.BtReceiveThread;
import app.autko.thread.BtSendThread;

public class BtService {

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter adapter;
    private final Handler handler;
    private volatile BtSendThread btSendThread;
    private volatile BtReceiveThread btReceiveThread;

    private BluetoothSocket socket;

    public BtService(final BluetoothAdapter adapter, final Handler handler) {
        this.adapter = adapter;
        this.handler = handler;
    }

    public void connect(final BluetoothDevice device) {
        try {
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
        } catch (final SecurityException | IOException exception) {
            throw new BtException("Failed to obtain Bluetooth socket.", exception);
        }

        try {
            adapter.cancelDiscovery();
        } catch (final SecurityException exception) {
            throw new BtException("Failed to cancel Bluetooth discovery.", exception);
        }

        final BtConnectThread btConnectThread = new BtConnectThread(socket, handler, (btSendThread, btReceiveThread) -> {
            this.btSendThread = btSendThread;
            this.btReceiveThread = btReceiveThread;
        });
        btConnectThread.start();
    }

    public void disconnect() {
        try {
            Log.d("BT SERVICE", "Interrupt send thread.");
            btSendThread.interrupt();
            Log.d("BT SERVICE", "Interrupt receive thread.");
            btReceiveThread.interrupt();
            Log.d("BT SERVICE", "Close socket.");
            socket.close();
        } catch (final IOException exception) {
            throw new BtException("Failed to close connection.", exception);
        }
    }

    public void send(final byte[] bytes) {
        btSendThread.getMyHandler().obtainMessage(BtMessageCodes.SEND_BYTES, bytes).sendToTarget();
    }

}
