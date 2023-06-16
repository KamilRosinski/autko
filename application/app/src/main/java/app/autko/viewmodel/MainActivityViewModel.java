package app.autko.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainActivityViewModel extends ViewModel {

    private final MutableLiveData<Boolean> connected = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> progress = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> btSupported = new MutableLiveData<>();
    private final MutableLiveData<Integer> btState = new MutableLiveData<>();

    public LiveData<Boolean> isConnected() {
        return connected;
    }

    public LiveData<Integer> getProgress() {
        return progress;
    }

    public void setProgress(final Integer progress) {
        this.progress.setValue(progress);
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
