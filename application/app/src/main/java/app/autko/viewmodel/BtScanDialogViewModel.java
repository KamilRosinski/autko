package app.autko.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BtScanDialogViewModel extends ViewModel {

    private final MutableLiveData<Boolean> scanning = new MutableLiveData<>(false);

    public LiveData<Boolean> isScanning() {
        return scanning;
    }

    public void setScanning(final boolean scanning) {
        this.scanning.setValue(scanning);
    }
}
