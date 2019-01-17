package com.udacity.lineker.wakemethere;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.udacity.lineker.wakemethere.database.PlaceEntry;
import com.udacity.lineker.wakemethere.widget.WakeMeThereService;
import com.udacity.lineker.wakemethere.widget.PlaceViewsFactory;
import com.udacity.lineker.wakemethere.widget.WidgetService;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class WakeMeWidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_WORD=
            "com.udacity.lineker.wakemethere.WORD";

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                        List<PlaceEntry> places, int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wake_me_widget_provider);

        int randomNumber=(int)(Math.random()*1000);
        Intent svcIntent=new Intent(context, WidgetService.class);
        svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId+randomNumber);
        svcIntent.putExtra(PlaceViewsFactory.EXTRA_ITEMS, getItems(places));
        svcIntent.putExtra(PlaceViewsFactory.EXTRA_APPWIDGET_ID_WAKEME, appWidgetId);
        svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(appWidgetId, R.id.words, svcIntent);

        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context,0, i,0);
        views.setOnClickPendingIntent(R.id.select_places,pi);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        WakeMeThereService.startActionUpdateWakeMeThereWidgets(context);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private static ArrayList<String> getItems(List<PlaceEntry> places) {
        ArrayList<String> items = new ArrayList<>();
        if (places != null) {
            for (PlaceEntry placeEntry : places) {
                String item = String.format("%s",
                        placeEntry.getName());
                items.add(item);
            }
        }
        return items;
    }

    public static void updateWakeMeThereWidgets(Context context, AppWidgetManager appWidgetManager, List<PlaceEntry> places, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, places, appWidgetId);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.words);
        }
    }
}

