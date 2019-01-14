package com.udacity.lineker.wakemethere;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.udacity.lineker.wakemethere.database.AppDatabase;
import com.udacity.lineker.wakemethere.database.AppExecutors;
import com.udacity.lineker.wakemethere.database.PlaceEntry;
import com.udacity.lineker.wakemethere.databinding.ActivityMainBinding;
import com.udacity.lineker.wakemethere.geofence.Geofencing;
import com.udacity.lineker.wakemethere.main.PlaceAdapter;
import com.udacity.lineker.wakemethere.main.PlaceClickCallback;
import com.udacity.lineker.wakemethere.main.PlacesViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;

    private ActivityMainBinding binding;
    private AppDatabase mDb;
    private PlaceAdapter placeAdapter;

    private GridLayoutManager mGridLayoutManager;
    private GoogleApiClient mClient;
    private Geofencing mGeofencing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        binding.addPlace.setOnClickListener(addPlaceListener);
        mDb = AppDatabase.getInstance(getApplicationContext());

        placeAdapter = new PlaceAdapter(placeClickCallback);

        mGridLayoutManager = new GridLayoutManager(this, getResources().getInteger(R.integer.place_list_columns));
        binding.recyclerview.setLayoutManager(mGridLayoutManager);
        binding.recyclerview.setAdapter(placeAdapter);

        mClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

        mGeofencing = new Geofencing(this, mClient);

        final PlacesViewModel viewModel =
                ViewModelProviders.of(this).get(PlacesViewModel.class);

        observeViewModel(viewModel);
    }

    private void observeViewModel(PlacesViewModel viewModel) {
        viewModel.getPlaceListObservable(mDb).observe(this, new Observer<List<PlaceEntry>>() {
            @Override
            public void onChanged(@Nullable List<PlaceEntry> placeEntries) {
                if (placeEntries == null || placeEntries.size() == 0) {

                } else {
                    final Map<String,PlaceEntry> map = new HashMap<String,PlaceEntry>();
                    List<String> guids = new ArrayList<String>();
                    for (PlaceEntry placeEntry : placeEntries) {
                        map.put(placeEntry.getPlaceId(), placeEntry);
                        guids.add(placeEntry.getPlaceId());
                    }

                    PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mClient,
                            guids.toArray(new String[guids.size()]));
                    placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
                        @Override
                        public void onResult(@NonNull PlaceBuffer places) {
                            List<PlaceEntry> placeEntryList = new ArrayList<>();
                            for (Place place : places) {
                                PlaceEntry placeEntry = map.get(place.getId());
                                String address = place.getAddress().toString();
                                String name = place.getName().toString();
                                if (isReadable(name)) {
                                    placeEntry.setAddress(address);
                                    placeEntry.setName(name);
                                } else {
                                    // put name on address
                                    placeEntry.setAddress(name);
                                    placeEntry.setName(address);
                                }
                                placeEntryList.add(placeEntry);
                            }
                            placeAdapter.setPlaceList(placeEntryList);
                            mGeofencing.updateGeofencesList(places, map);
                            mGeofencing.unRegisterAllGeofences();
                            mGeofencing.registerAllGeofences();
                        }
                    });
                }
            }
        });
    }


    View.OnClickListener addPlaceListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_FINE_LOCATION);
                return;
            }
            callPlacePicker();
        }
    };

    private void callPlacePicker() {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(MainActivity.this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Log.i(TAG, String.format("onActivityResult %s %s", requestCode, resultCode));
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            // Extract the place information from the API
            final String placeID = place.getId();
            Log.i(TAG, String.format("onActivityResult placeId %s", placeID));

            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    PlaceEntry placeEntry = mDb.placeDao().findPlaceByPlaceId(placeID);
                    if (placeEntry == null) {
                        placeEntry = new PlaceEntry();
                        placeEntry.setPlaceId(placeID);
                        placeEntry.setActive(true);
                        mDb.placeDao().insert(placeEntry);
                    } else {
                        placeEntry.setActive(true);
                        mDb.placeDao().update(placeEntry);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    callPlacePicker();

                } else {
                    Toast.makeText(this, R.string.permission_explanation, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private final PlaceClickCallback placeClickCallback = new PlaceClickCallback() {
        @Override
        public void onClick(final PlaceEntry placeEntry) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    PlaceEntry placeEntryUpdate = mDb.placeDao().findPlaceByPlaceId(placeEntry.getPlaceId());
                    if (placeEntryUpdate != null) {
                        placeEntryUpdate.setActive(!placeEntryUpdate.isActive());
                        mDb.placeDao().update(placeEntryUpdate);
                    }
                }
            });
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        final PlacesViewModel viewModel =
                ViewModelProviders.of(this).get(PlacesViewModel.class);

        observeViewModel(viewModel);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API Client Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "API Client Connection Failed!");
    }

    private boolean isReadable(String s) {
        int count = 0;
        for (int i = 0, len = s.length(); i < len; i++) {
            if (Character.isDigit(s.charAt(i))) {
                count++;
            }
        }
        return count < s.length()/2;
    }
}
