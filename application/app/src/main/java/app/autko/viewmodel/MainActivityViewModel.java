package app.autko.viewmodel;

import android.bluetooth.BluetoothAdapter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import app.autko.util.BtLabelProvider;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Integer> progress = new MutableLiveData<>();
    private final MutableLiveData<Boolean> btSupported = new MutableLiveData<>();
    private final MutableLiveData<Integer> btState = new MutableLiveData<>();

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public void setProgress(final Integer progress) {
        this.progress.postValue(progress);
    }

    public LiveData<Boolean> isBtSupported() {
        return btSupported;
    }

    public void setBtSupported(final Boolean btSupported) {
        this.btSupported.postValue(btSupported);
    }

    public LiveData<Integer> getBtState() {
        return btState;
    }

    public void setBtState(final int btState) {
        this.btState.postValue(btState);
    }

    public LiveData<String> getBtStateLabel() {
        return Transformations.map(btState, BtLabelProvider::getLabel);
    }

    public LiveData<Boolean> isBtDisabled() {
        return Transformations.map(btState, btState -> btState == BluetoothAdapter.STATE_OFF);
    }

}
