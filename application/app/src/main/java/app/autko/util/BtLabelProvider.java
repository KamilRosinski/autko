package app.autko.util;

import android.bluetooth.BluetoothAdapter;

import java.util.Map;

public class BtLabelProvider {

    private static final Map<Integer, String> BT_LABELS = Map.of(
            BluetoothAdapter.STATE_OFF, "BT disabled",
            BluetoothAdapter.STATE_TURNING_ON, "Enabling BT...",
            BluetoothAdapter.STATE_ON, "BT enabled",
            BluetoothAdapter.STATE_TURNING_OFF, "Disabling BT..."
    );

    public static String getLabel(final int btState) {
        return BT_LABELS.getOrDefault(btState, "unknown");
    }

}
