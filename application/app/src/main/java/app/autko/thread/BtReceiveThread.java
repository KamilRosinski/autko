package app.autko.thread;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import app.autko.constant.BtMessageCodes;
import app.autko.exception.BtException;

public class BtReceiveThread extends Thread {

    private static final int BUFFER_SIZE = 1024;

    private final InputStream stream;
    private final Handler handler;

    public BtReceiveThread(final InputStream stream,
                           final Handler handler) {
        super("BtReceiveThread");
        this.stream = stream;
        this.handler = handler;
    }

    @Override
    public void run() {
        byte[] buffer = new byte[BUFFER_SIZE];
        while (!Thread.interrupted()) {
            try {
                if (stream.available() > 0) {
                    final int bytesRead = stream.read(buffer);
                    if (bytesRead > 0) {
                        handler.obtainMessage(BtMessageCodes.BYTES_RECEIVED, Arrays.copyOfRange(buffer, 0, bytesRead)).sendToTarget();
                    }
                }
            } catch (final IOException exception) {
                handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to read data.", exception)).sendToTarget();
                return;
            }
        }
        Log.d("BT RECEIVE THREAD", "Goodbye.");
    }
}
