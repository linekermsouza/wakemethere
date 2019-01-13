package com.udacity.lineker.wakemethere.main;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.android.gms.location.places.PlaceBuffer;
import com.udacity.lineker.wakemethere.R;

import com.udacity.lineker.wakemethere.database.PlaceEntry;
import com.udacity.lineker.wakemethere.databinding.PlaceListItemBinding;

import java.util.List;


public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder> {

    private List<PlaceEntry> placeList;

    @Nullable
    private final PlaceClickCallback placeClickCallback;

    public PlaceAdapter(@Nullable PlaceClickCallback placeClickCallback) {
        this.placeClickCallback = placeClickCallback;
    }

    public void setPlaceList(final List<PlaceEntry> placeList) {
        this.placeList = placeList;
        if (placeList != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }

    @Override
    public PlaceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PlaceListItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.place_list_item,
                        parent, false);

        binding.setCallback(placeClickCallback);
        return new PlaceViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        holder.binding.setPlace(placeList.get(position));
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return placeList == null ? 0 : placeList.size();
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        final PlaceListItemBinding binding;

        public PlaceViewHolder(PlaceListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}