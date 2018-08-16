package com.example.baskin.travelandentertainmentsearch;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ResultsActivity extends Activity implements PlacesArrayAdapter.PlaceClickedListener {

    private String npt;
    private ArrayList<ArrayList<JSONObject>> pages;
    private int current_page;
    private RecyclerView recyclerView;
    private PlacesArrayAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;


    private Button prevButton;
    private Button nextButton;

    private Api api;
    private FavoriteList favList;

    private LinearLayout ResultListNoResults;
    private LinearLayout ResultListYesResults;

    public void onPlaceFavClick(JSONObject place, int position) {
        favList.toggle(place, this);
        adapter.notifyDataSetChanged();

    }

    public void onPlaceClick(JSONObject place, int position) {
        Intent it = new Intent(this, DetailsActivity.class);
        it.putExtra("place", place.toString());
        startActivity(it);
    }

    private void noresults(boolean isEmpty) {
        if(isEmpty) {
            ResultListNoResults.setVisibility(View.VISIBLE);
            ResultListYesResults.setVisibility(View.GONE);
        } else {
            ResultListNoResults.setVisibility(View.GONE);
            ResultListYesResults.setVisibility(View.VISIBLE);

        }
    }

    private boolean hasNext() {
        return current_page < pages.size() - 1 || npt != null;
    }

    private boolean hasPrevious() {
        return current_page > 0;
    }

    private ArrayList<JSONObject> updateNPT_getPlacesArrayList(JSONObject placesResponse) {
        ArrayList<JSONObject> plcs = new ArrayList<JSONObject>();

        if(placesResponse.has("next_page_token")) {
            try {
                npt = placesResponse.getString("next_page_token");
            } catch(JSONException exp) {
                npt = null;
            }
        } else {
            npt = null;
        }

        if(placesResponse.has("places")) {
            try {
                JSONArray places = placesResponse.getJSONArray("places");

                for (int i = 0; i < places.length(); i++) {
                    JSONObject pl = places.getJSONObject(i);
                    plcs.add(places.getJSONObject(i));
                }

            } catch(JSONException exp) {
                plcs.clear();
            }
        }

        return plcs;
    }

    private void loadNextWithNPT() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Fetching next page");
        pd.show();
        pd.setCancelable(false);

        String requestUrl = api.buildNPTReq(npt);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    pd.dismiss();
                    try {
                        JSONObject results = new JSONObject(response);
                        ArrayList<JSONObject> places = updateNPT_getPlacesArrayList(results);
                        if(places.size() > 0) {
                            pages.add(places);
                            current_page++;
                            showPage(current_page);
                        } else {
                            // should no happen
                            noresults(true);
                        }
                    } catch(JSONException exp) {
                        // not a json.
                        noresults(true);
                    }

                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pd.dismiss();
                Toast.makeText(getApplicationContext(), "Request error.", Toast.LENGTH_SHORT).show();
                System.out.println(error.toString());
            }
        });
        api.add(stringRequest);
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
        } else if(npt != null) {
            loadNextWithNPT();
        }
    }

    private void showPage(int page) {
        current_page = page;
        adapter.replace(pages.get(page));
        updateButtons();
        noresults(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        ResultListNoResults = (LinearLayout) findViewById(R.id.ResultListNoResults);
        ResultListYesResults = (LinearLayout) findViewById(R.id.ResultListYesResults);
        api = Api.getInstance(this);
        favList = FavoriteList.getInstance(this);
        prevButton = (Button) findViewById(R.id.resultsPrevButton);
        nextButton = (Button) findViewById(R.id.resultsNextButton);
        npt = null;
        pages = new ArrayList<ArrayList<JSONObject>>();
        adapter = new PlacesArrayAdapter(new ArrayList<JSONObject>(), this, this);
        recyclerView = (RecyclerView) findViewById(R.id.resultList);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);


        JSONObject results;
        try {
            results = new JSONObject(getIntent().getStringExtra("results"));
            ArrayList<JSONObject> places = updateNPT_getPlacesArrayList(results);
            if(places.size() > 0) {
                pages.add(places);
                showPage(0);
            } else {
                noresults(true);
            }
        } catch(JSONException exp) {
            // should not happen
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);




        getActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24px);

        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");

        if(actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if(title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.notifyDataSetChanged();

    }
}
