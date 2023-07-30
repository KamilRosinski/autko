package app.autko.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

import app.autko.constant.BtMessageCodes;
import app.autko.exception.BtException;

public class BtConnectThread extends Thread {

    private final BluetoothSocket socket;
    private final Handler handler;
    private final Runnable onSuccess;

    public BtConnectThread(final BluetoothSocket socket,
                           final Handler handler,
                           final Runnable onSuccess) {
        super("BtConnectThread");
        this.socket = socket;
        this.handler = handler;
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        try {
            socket.connect();
        } catch (final IOException | SecurityException exception) {
            closeSocket();
            handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to establish connection.", exception)).sendToTarget();
            return;
        }

        onSuccess.run();
        Log.d("BT CONNECT THREAD", "Goodbye.");
    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (final IOException exception) {
            handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to close socket after exception.", exception)).sendToTarget();
        }
    }

}
