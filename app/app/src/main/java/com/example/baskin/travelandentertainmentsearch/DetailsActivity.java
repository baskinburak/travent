package com.example.baskin.travelandentertainmentsearch;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public class DetailsActivity extends FragmentActivity {

    private static final int NUM_PAGES = 4;
    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;
    private Fragment[] mPages;


    private JSONObject place;
    private FavoriteList favList;

    private MenuItem detailsActionBarShare;
    private MenuItem detailsActionBarFav;

    private Context ctx;

    Api api;

    private JSONObject detailsResponse = new JSONObject();
    private ArrayList<Bitmap> placePhotos = new ArrayList<Bitmap>();
    private JSONArray yelpReviews = new JSONArray();

    private ProgressDialog pd;

    private int advertCount = 0;
    private Semaphore advertMutex = new Semaphore(1, true);

    private void afterAdvert() {
        try {
            advertMutex.acquire();
        } catch(Exception exp) {

        }
        advertCount++;
        if(advertCount == 3) {
            ActionBar actionBar = getActionBar();
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                    mPager.setCurrentItem(tab.getPosition());
                }
                public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // hide the given tab
                }

                public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                    // probably ignore this event
                }
            };
            actionBar.addTab(actionBar.newTab().setText("INFO").setIcon(getResources().getDrawable(R.drawable.ic_info)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText("PHOTOS").setIcon(getResources().getDrawable(R.drawable.ic_photos)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText("MAP").setIcon(getResources().getDrawable(R.drawable.ic_map)).setTabListener(tabListener));
            actionBar.addTab(actionBar.newTab().setText("REVIEWS").setIcon(getResources().getDrawable(R.drawable.ic_reviews)).setTabListener(tabListener));

            advertiseAll();
            pd.dismiss();
        }
        advertMutex.release();
    }

    private void advertiseAll() {
        ((InfoFragment)mPages[0]).setDetails(detailsResponse);
        ((MapFragment)mPages[2]).setDetails(detailsResponse);
        ((ReviewsFragment)mPages[3]).setDetails(detailsResponse);
        ((ReviewsFragment)mPages[3]).setYelpReviews(yelpReviews);
        ((PhotosFragment)mPages[1]).setPhotos(placePhotos);
    }

    private void advertiseResponse() {
        afterAdvert();
    }

    private void advertiseYelp() {
        afterAdvert();
    }

    private void advertisePhotos() {
        afterAdvert();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        final ActionBar actionBar = getActionBar();
        ctx = this;
        api = Api.getInstance(this);
        placePhotos = new ArrayList<Bitmap>();

        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");

        if(actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if(title != null) {
                title.setTextColor(Color.WHITE);
            }
        }

        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        try {
            place = new JSONObject(getIntent().getStringExtra("place"));
        } catch(JSONException exp) {
            Toast.makeText(this, "Internal JSON error.", Toast.LENGTH_SHORT).show();
            return;
        }

        favList = FavoriteList.getInstance(this);

        String place_name = "";

        try {
            place_name = place.getString("name");
        } catch(JSONException exp) {

        }

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(place_name);


        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24px);

        pd = new ProgressDialog(this);
        pd.setMessage("Fetching details");
        pd.show();
        pd.setCancelable(false);





        mPager = (ViewPager) findViewById(R.id.main_pager);
        mPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
            }
        });
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPages = new Fragment[4];
        mPages[0] = new InfoFragment();
        mPages[1] = new PhotosFragment();
        mPages[2] = new MapFragment();
        mPages[3] = new ReviewsFragment();




        final Semaphore photoMutex = new Semaphore(1, true);


        String id = "";
        try {
            id = place.getString("id");
        } catch(JSONException exp){

        }


        final GeoDataClient geoDataClient = Places.getGeoDataClient(ctx, null);

        geoDataClient.getPlacePhotos(id).addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            private int load_count;

            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                if(task.isSuccessful()) {
                    load_count = 0;
                    PlacePhotoMetadataResponse photos = task.getResult();
                    final PlacePhotoMetadataBuffer photosMeta = photos.getPhotoMetadata();
                    if(photosMeta.getCount() == 0) {
                        advertisePhotos();
                    }
                    for(int i=0; i<photosMeta.getCount(); i++) {

                        PlacePhotoMetadata photo = photosMeta.get(i);

                        geoDataClient.getPhoto(photo).addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
                            @Override
                            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                                if(task.isSuccessful()) {
                                    try {
                                        photoMutex.acquire();
                                    } catch(InterruptedException exp) {

                                    }

                                    PlacePhotoResponse photo = task.getResult();

                                    placePhotos.add(photo.getBitmap());


                                    if(placePhotos.size() + load_count == photosMeta.getCount()) {
                                        advertisePhotos();
                                        photosMeta.release();
                                    }
                                    photoMutex.release();
                                } else {
                                    try {
                                        photoMutex.acquire();
                                    } catch(InterruptedException exp) {

                                    }
                                    load_count++;

                                    if(placePhotos.size() + load_count == photosMeta.getCount()) {
                                        advertisePhotos();
                                        photosMeta.release();
                                    }
                                    photoMutex.release();
                                }
                            }
                        });
                    }
                } else {

                }
            }
        });



        String requestUrl = api.buildPlaceDetailsReq(id);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            detailsResponse = new JSONObject(response);
                            detailsResponse = detailsResponse.getJSONObject("result");


                            String yelpReqUrl = api.buildYelpReviewsReq(detailsResponse);


                            StringRequest stringRequest2 = new StringRequest(Request.Method.GET, yelpReqUrl,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            try {
                                                yelpReviews = new JSONArray(response);
                                            } catch(JSONException exp) {
                                                // not a json.
                                                Toast.makeText(ctx, "Yelp response is not a JSON.", Toast.LENGTH_SHORT).show();
                                            }
                                            advertiseYelp();

                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(ctx, "Yelp request error.", Toast.LENGTH_SHORT).show();
                                    advertiseYelp();
                                }
                            });

                            api.add(stringRequest2);

                        } catch(JSONException exp) {
                            // not a json.
                            Toast.makeText(ctx, "Details response is not a JSON.", Toast.LENGTH_SHORT).show();
                        }
                        advertiseResponse();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(ctx, "Details request error.", Toast.LENGTH_SHORT).show();
                advertiseResponse();
                advertiseYelp();
            }
        });

        api.add(stringRequest);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        detailsActionBarShare = (MenuItem)menu.findItem(R.id.detailsActionBarShare);
        detailsActionBarFav = (MenuItem)menu.findItem(R.id.detailsActionBarFav);


        String id = "";

        try {
            id = place.getString("id");
        } catch (JSONException exp) {

        }

        if(favList.exists(id)) {
            detailsActionBarFav.setIcon(R.drawable.ic_heart_fill_white);
        } else {
            detailsActionBarFav.setIcon(R.drawable.ic_heart_outline_white);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.detailsActionBarShare:
                String name = "";
                try {
                    name = detailsResponse.getString("name");
                } catch(JSONException e) {

                }

                String address = "";
                try {
                    address = detailsResponse.getString("formatted_address");
                } catch(JSONException e) {

                }

                String website = "";

                try {
                    website = detailsResponse.getString("website");
                } catch(JSONException e) {

                }

                String tweet = "Check out " + name + " located at " + address;

                if(!website.equals(""))
                    tweet += "\nWebsite: " + website;
                try {
                    Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=" + URLEncoder.encode(tweet, "UTF-8")));
                    startActivity(it);
                } catch(UnsupportedEncodingException exp) {

                }
                return true;
            case R.id.detailsActionBarFav:
                favList.toggle(place, this);
                String id = "";
                try {
                    id = place.getString("id");
                } catch(JSONException exp) {

                }
                if(favList.exists(id)) {
                    detailsActionBarFav.setIcon(R.drawable.ic_heart_fill_white);
                } else {
                    detailsActionBarFav.setIcon(R.drawable.ic_heart_outline_white);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.details_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mPages[position];
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

    }
}
