package com.udacity.lineker.wakemethere.geofence;

/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.AlarmClock;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.udacity.lineker.wakemethere.R;
import com.udacity.lineker.wakemethere.database.AppDatabase;
import com.udacity.lineker.wakemethere.database.AppExecutors;
import com.udacity.lineker.wakemethere.database.PlaceEntry;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();
    private AppDatabase mDb;

    /***
     * Handles the Broadcast message sent when the Geofence Transition is triggered
     * Careful here though, this is running on the main thread so make sure you start an AsyncTask for
     * anything that takes longer than say 10 second to run
     *
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.i(TAG, "onReceive called");
        // Get the Geofence Event from the Intent sent through
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code : %d", geofencingEvent.getErrorCode()));
            return;
        }
        // Call getGeofenceTransition to get the transition type and use AudioManager to set the
        // phone ringer mode based on the transition type. Feel free to create a helper method (setRingerMode)
        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        // Check which transition type has triggered this event
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            final List<Geofence> triggerList = geofencingEvent.getTriggeringGeofences();

            mDb = AppDatabase.getInstance(context.getApplicationContext());
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    for (Geofence geofence : triggerList) {
                        String placeId = geofence.getRequestId();
                        Log.i(TAG, "onReceive called GEOFENCE_TRANSITION_ENTER placeId " + placeId);
                        PlaceEntry placeEntry = mDb.placeDao().findPlaceByPlaceId(placeId);
                        if (placeEntry != null) {
                            if (placeEntry.isActive()) {
                                placeEntry.setActive(false);
                                mDb.placeDao().update(placeEntry);

                                Log.i(TAG, "onReceive called GEOFENCE_TRANSITION_ENTER");
                                setAlarm(context);
                            }
                        }
                    }

                }
            });

        } else {
            // Log the error.
            Log.e(TAG, String.format("Unknown transition : %d", geofenceTransition));
            // No need to do anything else
            return;
        }

    }

    private void setAlarm(Context context) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.add(Calendar.MINUTE, 1);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        Intent i = new Intent(AlarmClock.ACTION_SET_ALARM);
        i.putExtra(AlarmClock.EXTRA_HOUR, hour);
        i.putExtra(AlarmClock.EXTRA_MINUTES, minute+1);
        i.putExtra(AlarmClock.EXTRA_SKIP_UI, true);
        i.putExtra(AlarmClock.EXTRA_MESSAGE, context.getString(R.string.wakememsg));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}

