package com.example.baskin.travelandentertainmentsearch;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


/**
 * A simple {@link Fragment} subclass.
 */
public class PhotosFragment extends Fragment {


    ArrayList<Bitmap> photos;

    PhotoAdapter adapter;

    private Semaphore mtx = new Semaphore(1, true);

    View root;

    public void setPhotos(ArrayList<Bitmap> p) {
        try {
            mtx.acquire();
        } catch(Exception e) {

        }
        photos = p;
        if(adapter != null) {
            adapter.clear();
            adapter.addAll(photos);
            adapter.notifyDataSetChanged();
        }

        showCorrectPage();
        mtx.release();
    }

    private void showCorrectPage() {
        if(root != null) {
            if (photos.size() > 0) {
                root.findViewById(R.id.PhotoListNoPhotos).setVisibility(View.GONE);
                root.findViewById(R.id.photoList).setVisibility(View.VISIBLE);
            } else {
                root.findViewById(R.id.PhotoListNoPhotos).setVisibility(View.VISIBLE);
                root.findViewById(R.id.photoList).setVisibility(View.GONE);
            }
        }
    }

    public PhotosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        try {
            mtx.acquire();
        } catch(Exception e) {

        }
        if(photos == null) {
            photos = new ArrayList<Bitmap>();
        }

        adapter = new PhotoAdapter(getActivity(), photos);
        root = inflater.inflate(R.layout.fragment_photos, container, false);
        ListView list = (ListView) root.findViewById(R.id.photoList);

        list.setAdapter(adapter);
        showCorrectPage();
        mtx.release();
        return root;
    }

}
