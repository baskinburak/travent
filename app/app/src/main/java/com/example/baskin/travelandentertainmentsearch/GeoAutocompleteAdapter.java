package com.example.baskin.travelandentertainmentsearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class GeoAutocompleteAdapter extends ArrayAdapter {

    private ArrayList<String> suggestions;
    private GeoFilter filter = new GeoFilter();
    private GeoDataClient geoDataClient;
    private AutocompleteFilter autoCompleteFilter;

   /* public interface geoClickedListener {
        public void geoClicked(String val);
    }

    geoClickedListener listener;*/

    public GeoAutocompleteAdapter(Context ctx) {
        super(ctx, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        geoDataClient = Places.getGeoDataClient(ctx, null);

        AutocompleteFilter.Builder filterBuilder = new AutocompleteFilter.Builder();
        filterBuilder.setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE);

        autoCompleteFilter = filterBuilder.build();

        suggestions = new ArrayList<String>();
    }

   /* public GeoAutocompleteAdapter(Context ctx, geoClickedListener L) {
        super(ctx, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        geoDataClient = Places.getGeoDataClient(ctx, null);

        AutocompleteFilter.Builder filterBuilder = new AutocompleteFilter.Builder();
        filterBuilder.setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS);

        autoCompleteFilter = filterBuilder.build();

        suggestions = new ArrayList<String>();

        listener = L;
    }*/

    @Override
    public int getCount() {
        return suggestions.size() + 1;
    }

    @Override
    public String getItem(int idx) {
        return suggestions.get(idx);
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        if(position == suggestions.size()) {
            // powered by google
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.powered_by_google, parent, false);

        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);

            TextView tv = view.findViewById(android.R.id.text1);

            tv.setText(suggestions.get(position));


           /* view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.geoClicked(suggestions.get(position));
                }
            });*/
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        return position != suggestions.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    public class GeoFilter extends Filter {

        private Semaphore filterSem = new Semaphore(0, true);

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            System.out.println(prefix);
            final FilterResults results = new FilterResults();
            if(prefix == null || prefix.length() == 0) {
                results.values = null;
                filterSem.release();
            } else {
                String prefixStr = prefix.toString().toLowerCase();
                Task<AutocompletePredictionBufferResponse> task =
                        geoDataClient.getAutocompletePredictions(prefixStr, null, autoCompleteFilter);
                task.addOnCompleteListener(new OnCompleteListener<AutocompletePredictionBufferResponse>() {
                    @Override
                    public void onComplete(Task<AutocompletePredictionBufferResponse> task) {
                        if (task.isSuccessful()) {
                            results.values = new ArrayList<String>();
                            AutocompletePredictionBufferResponse predictions = task.getResult();
                            for (AutocompletePrediction prediction : predictions) {
                                ((ArrayList<String>)results.values).add(prediction.getFullText(null).toString());
                            }
                            predictions.release();
                        } else {
                            results.values = null;
                        }
                        filterSem.release();
                    }
                });

            }
            try {
                filterSem.acquire();
            } catch(InterruptedException e) {

            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if(results.values == null || ((ArrayList<String>)results.values).size() == 0 ) {
                suggestions.clear();
            } else {
                suggestions = (ArrayList<String>)results.values;
            }

            if(suggestions.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }


}
