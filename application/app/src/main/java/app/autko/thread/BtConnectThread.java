package app.autko.thread;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;
import java.util.function.BiConsumer;

import app.autko.constant.BtMessageCodes;
import app.autko.exception.BtException;

public class BtConnectThread extends Thread {

    private final BluetoothSocket socket;
    private final Handler handler;
    private final BiConsumer<BtSendThread, BtReceiveThread> onSuccess;

    public BtConnectThread(final BluetoothSocket socket,
                           final Handler handler,
                           final BiConsumer<BtSendThread, BtReceiveThread> onSuccess) {
        this.socket = socket;
        this.handler = handler;
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        try {
            socket.connect();

            final BtSendThread btSendThread = new BtSendThread(socket.getOutputStream(), handler);
            btSendThread.start();

            final BtReceiveThread btReceiveThread = new BtReceiveThread(socket.getInputStream(), handler);
            btReceiveThread.start();

            onSuccess.accept(btSendThread, btReceiveThread);
        } catch (final IOException | SecurityException exception) {
            closeSocket();
            handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to establish connection.", exception)).sendToTarget();
        }

    }

    private void closeSocket() {
        try {
            socket.close();
        } catch (final IOException exception) {
            handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to close socket after exception.", exception)).sendToTarget();
        }
    }

}
