package com.example.baskin.travelandentertainmentsearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class PhotoAdapter extends ArrayAdapter<Bitmap> {
    public PhotoAdapter(Context ctx, ArrayList<Bitmap> arr) {
        super(ctx,0, arr);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bitmap bmp = getItem(position);

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.image_item, parent, false);
        }

        ImageView img = (ImageView) convertView.findViewById(R.id.placePhoto);

        img.setImageBitmap(bmp);

        return convertView;
    }
}
