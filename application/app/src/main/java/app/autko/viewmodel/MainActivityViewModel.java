package app.autko.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> btSupported = new MutableLiveData<>();
    private final MutableLiveData<Integer> btState = new MutableLiveData<>();

    public LiveData<Boolean> isConnected() {
        return connected;
    }

    public void setConnected(final boolean connected) {
        this.connected.setValue(connected);
    }

    public LiveData<Boolean> isBtSupported() {
        return btSupported;
    }

    public void setBtSupported(final Boolean btSupported) {
        this.btSupported.setValue(btSupported);
    }

    public void setBtState(final int btState) {
        this.btState.postValue(btState);
    }

}
