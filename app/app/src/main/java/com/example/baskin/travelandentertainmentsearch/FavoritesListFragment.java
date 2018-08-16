package com.example.baskin.travelandentertainmentsearch;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.json.JSONObject;

import java.util.ArrayList;


public class FavoritesListFragment extends Fragment implements PlacesArrayAdapter.PlaceClickedListener {

    private FavoriteList favList;
    private int current_page;
    private ArrayList<ArrayList<JSONObject>> pages;

    private Button prevButton;
    private Button nextButton;


    private RecyclerView recyclerView;
    private PlacesArrayAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private LinearLayout FavListNoResults;
    private LinearLayout FavListYesResults;

    public FavoritesListFragment() {
        // Required empty public constructor
    }

    public void onPlaceClick(JSONObject place, int position) {
        Intent it = new Intent(getActivity(), DetailsActivity.class);
        it.putExtra("place", place.toString());
        startActivity(it);
    }

    public void onPlaceFavClick(JSONObject place, int position) {
        favList.toggle(place, getActivity());
        repage();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        current_page = 0;
        favList = FavoriteList.getInstance(getActivity());

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View favListV =  inflater.inflate(R.layout.fragment_favorites_list, container, false);
        prevButton = (Button) favListV.findViewById(R.id.favsPrevButton);
        nextButton = (Button) favListV.findViewById(R.id.favsNextButton);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPrevious(v);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadNext(v);
            }
        });

        adapter = new PlacesArrayAdapter(new ArrayList<JSONObject>(), getActivity(), this);
        recyclerView = (RecyclerView) favListV.findViewById(R.id.favList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        FavListNoResults = (LinearLayout) favListV.findViewById(R.id.FavListNoResults);
        FavListYesResults = (LinearLayout) favListV.findViewById(R.id.FavListYesResults);

        return favListV;
    }

    private boolean hasNext() {
        return current_page < pages.size() - 1;
    }

    private boolean hasPrevious() {
        return current_page > 0;
    }

    private void updateButtons() {
        if(hasPrevious()) {
            prevButton.setEnabled(true);
        } else {
            prevButton.setEnabled(false);
        }

        if(hasNext()) {
            nextButton.setEnabled(true);
        } else {
            nextButton.setEnabled(false);
        }
    }

    public void loadPrevious(View view) {
        if(current_page > 0) {
            current_page--;
            showPage(current_page);
        }
    }

    public void loadNext(View view) {
        if(current_page < pages.size() - 1) {
            current_page++;
            showPage(current_page);
        }
    }

    private void showPage(int page) {
        if(current_page >= 0) {
            current_page = page;
            adapter.replace(pages.get(page));
            FavListNoResults.setVisibility(View.GONE);
            FavListYesResults.setVisibility(View.VISIBLE);
        } else {
            FavListNoResults.setVisibility(View.VISIBLE);
            FavListYesResults.setVisibility(View.GONE);
        }
        updateButtons();
    }

    private void repage() {
        pages = favList.paginate();
        if(current_page == -1)
            current_page = 0;

        current_page = Math.min(current_page, pages.size()-1);


        showPage(current_page);

    }

    @Override
    public void onResume() {
        super.onResume();

        repage();
    }

}
