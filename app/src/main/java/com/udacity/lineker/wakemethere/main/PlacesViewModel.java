package com.udacity.lineker.wakemethere.main;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;

import com.udacity.lineker.wakemethere.database.AppDatabase;
import com.udacity.lineker.wakemethere.database.PlaceEntry;

import java.util.List;

public class PlacesViewModel extends AndroidViewModel {

    public PlacesViewModel(Application application) {
        super(application);
    }

    public LiveData<List<PlaceEntry>> getPlaceListObservable(AppDatabase database) {
        return database.placeDao().loadAll();
    }
}


