package app.autko.thread;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import app.autko.constant.BtMessageCodes;
import app.autko.exception.BtException;

public class BtSendThread extends Thread {

    private final OutputStream stream;
    private final Handler handler;

    private volatile Handler myHandler = null;

    public BtSendThread(final OutputStream stream,
                        final Handler handler) {
        super("BtSendThread");
        this.stream = stream;
        this.handler = handler;
    }

    @Override
    public void run() {
        Looper.prepare();

        myHandler = new Handler(Looper.myLooper()) {
            @Override
            public void handleMessage(final Message message) {
                switch (message.what) {
                    case BtMessageCodes.SEND_BYTES -> {
                        try {
                            final byte[] bytes = (byte[]) message.obj;
                            Log.d("BT SEND THREAD", "Send bytes: " + Arrays.toString(bytes) + ".");
                            stream.write(bytes);
                        } catch (final IOException exception) {
                            handler.obtainMessage(BtMessageCodes.EXCEPTION, new BtException("Failed to send data.", exception)).sendToTarget();
                        }
                    }
                    case BtMessageCodes.DISCONNECT -> {
                        Log.d("BT SEND THREAD", "Quit looper.");
                        Looper.myLooper().quit();
                    }
                }
            }
        };

        Looper.loop();
        Log.d("BT SEND THREAD", "Goodbye.");
    }

    public Handler getMyHandler() {
        return myHandler;
    }

}
