package com.cforlando.streetartandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cforlando.streetartandroid.Helpers.ParseRecyclerQueryAdapter;
import com.cforlando.streetartandroid.Models.Installation;
import com.parse.ParseException;
import com.parse.ParseQueryAdapter;

/**
 * Created by benba on 6/2/2016.
 */


public class InstallationAdapter extends ParseRecyclerQueryAdapter<Installation, com.cforlando.streetartandroid.InstallationHolder> {


    private final OnItemClickListener listener;
    private Context mContext;

    public InstallationAdapter(boolean hasStableIds, OnItemClickListener listener) {
        super(Installation.class, hasStableIds);
        this.listener = listener;
    }

    public InstallationAdapter(ParseQueryAdapter.QueryFactory<Installation> factory, OnItemClickListener listener, boolean hasStableIds) {
        super(factory, hasStableIds);
        this.listener = listener;

    }

    @Override
    public InstallationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.list_item_installation, parent, false);
        return new InstallationHolder(view);
    }

    @Override
    public void onBindViewHolder(com.cforlando.streetartandroid.InstallationHolder holder, int position) {
        try {
            holder.bind(getItem(position), listener);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Installation installation);
    }

}

