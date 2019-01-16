package com.udacity.lineker.wakemethere.main;

import com.udacity.lineker.wakemethere.database.PlaceEntry;

public interface PlaceClickCallback {
    int ACTION_SWITCH = 0;
    int ACTION_EDIT = 1;
    int ACTION_REMOVE = 2;
    void onClick(PlaceEntry place, int action);
}