package com.udacity.lineker.wakemethere.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.udacity.lineker.wakemethere.WakeMeWidgetProvider;
import com.udacity.lineker.wakemethere.database.AppDatabase;
import com.udacity.lineker.wakemethere.database.PlaceEntry;

import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class WakeMeThereService extends IntentService {

    public static final String ACTION_UPDATE_WAKE_ME_THERE_WIDGETS = "com.udacity.lineker.wakemethere.action.update_wake_me_there_widgets";

    public WakeMeThereService() {
        super("WakeMeThereService");
    }

    public static void startActionUpdateWakeMeThereWidgets(Context context) {
        Intent intent = new Intent(context, WakeMeThereService.class);
        intent.setAction(ACTION_UPDATE_WAKE_ME_THERE_WIDGETS);
        context.startService(intent);
    }

    /**
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_UPDATE_WAKE_ME_THERE_WIDGETS.equals(action)) {
                handleActionUpdateWakeMeThereWidgets();
            }
        }
    }


    private void handleActionUpdateWakeMeThereWidgets() {
        AppDatabase database = AppDatabase.getInstance(this.getApplicationContext());
        List<PlaceEntry> placeEntries = database.placeDao().loadAllActivesSync();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(WakeMeThereService.this.getApplicationContext());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(WakeMeThereService.this, WakeMeWidgetProvider.class));
        //Now update all widgets
        WakeMeWidgetProvider.updateWakeMeThereWidgets(WakeMeThereService.this, appWidgetManager, placeEntries, appWidgetIds);
    }
}
