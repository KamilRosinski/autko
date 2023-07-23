package app.autko.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BtDeviceArrayAdapter extends ArrayAdapter<BluetoothDevice> {

    public BtDeviceArrayAdapter(final Context context) {
        super(context, -1);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        final View view = convertView != null
                ? convertView
                : LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_single_choice, parent, false);

        Log.d("LIST ADAPTER", "Position: " + position + ", item: " + getItem(position) + ".");
        final BluetoothDevice device = getItem(position);
        final String text = device.getName() != null
                ? device.getName() + " (" + device.getAddress() + ")"
                : device.getAddress();

        ((TextView) view).setText(text);

        return view;
    }
}
