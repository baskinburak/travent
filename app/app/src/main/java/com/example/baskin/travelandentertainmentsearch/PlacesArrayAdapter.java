package com.example.baskin.travelandentertainmentsearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class PlacesArrayAdapter extends RecyclerView.Adapter<PlacesArrayAdapter.ViewHolder> {
    private ArrayList<JSONObject> places;
    private FavoriteList favList;
    private Context context;

    private PlaceClickedListener clickListener;

    public interface PlaceClickedListener {
        void onPlaceClick(JSONObject place, int position);
        void onPlaceFavClick(JSONObject place, int position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout layout;

        public ViewHolder(LinearLayout v) {
            super(v);
            layout = v;
        }

    }

    public PlacesArrayAdapter(ArrayList<JSONObject> plc, Context ctx, PlaceClickedListener listen) {
        places = plc;
        favList = FavoriteList.getInstance(ctx.getApplicationContext());
        context = ctx;
        clickListener = listen;
    }

    @Override
    public PlacesArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.results_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        JSONObject place = places.get(position);



        LinearLayout convertView = holder.layout;

        ImageView categoryImage = (ImageView) convertView.findViewById(R.id.categoryImage);
        TextView placeName = (TextView) convertView.findViewById(R.id.placeName);
        TextView placeAddress = (TextView) convertView.findViewById(R.id.placeAddress);
        ImageView favImage = (ImageView) convertView.findViewById(R.id.favImage);
        try {
            Picasso.get().load(place.getString("icon")).into(categoryImage);
        } catch(JSONException exp) {

        }

        String id = "";
        try {
            id = places.get(position).getString("id");
        } catch(JSONException exp) {

        }

        if(favList.exists(id))
            favImage.setImageResource(R.drawable.ic_heart_fill_red);
        else {
            favImage.setImageResource(R.drawable.ic_heart_outline_black);
        }

        try {
            placeName.setText(place.getString("name"));
        } catch(JSONException exp) {

        }

        try {
            placeAddress.setText(place.getString("address"));
        } catch(JSONException exp) {

        }

        favImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onPlaceFavClick(places.get(position), position);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickListener.onPlaceClick(places.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return places.size();
    }

    public void replace(ArrayList<JSONObject> newdata) {
        places = newdata;
        notifyDataSetChanged();
    }
}
