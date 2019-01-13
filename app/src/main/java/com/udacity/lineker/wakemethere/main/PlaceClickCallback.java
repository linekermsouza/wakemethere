package com.udacity.lineker.wakemethere.main;

import com.udacity.lineker.wakemethere.database.PlaceEntry;

public interface PlaceClickCallback {
    void onClick(PlaceEntry place);
}