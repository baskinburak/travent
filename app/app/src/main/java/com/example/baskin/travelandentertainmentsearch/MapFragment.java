package com.example.baskin.travelandentertainmentsearch;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback/*implements GeoAutocompleteAdapter.geoClickedListener*/{

    JSONObject details;
    View root;
    Spinner travelModeSpinner;
    String travelMode;
    AutoCompleteTextView locationBox;
    SupportMapFragment mapFragment;

    String name = "";
    GoogleMap map;
    Polyline directions;

    Marker fromMarker;

    double lat = 0;
    double lon = 0;

    Api api;
    Context ctx;

    String last_text;

    private Semaphore mtx = new Semaphore(1, true);

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng pt = new LatLng(lat, lon);
        googleMap.addMarker(new MarkerOptions().position(pt).title(name)).showInfoWindow();
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pt, 12));
        map = googleMap;
    }

    public void setDetails(JSONObject d) {
        try {
            mtx.acquire();
        } catch(InterruptedException exp) {

        }

        details = d;

        try {
            lat = details.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            lon = details.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
        } catch(JSONException exp) {

        }


        try {
            name = details.getString("name");
        } catch(JSONException exp) {

        }

        if(root != null) {
            putMarker();
        }

        mtx.release();
    }

    private void putMarker() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void updatePath() {
        if(last_text != null) {
            updatePath(last_text);
        }
    }

    private void updatePath(final String from) {

        last_text = from;

        if(directions != null) {
            directions.remove();
            fromMarker.remove();
        }

        String requestUrl = api.buildDirectionsReq(from, lat, lon, travelMode);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject resp;
                    try {
                        resp = new JSONObject(response);
                        if(resp.has("routes")) {
                            try {
                                JSONObject route = resp.getJSONArray("routes").getJSONObject(0);
                                JSONArray legs = route.getJSONArray("legs");
                                PolylineOptions po = new PolylineOptions();
                                po.clickable(false);
                                JSONObject step = new JSONObject();

                                LatLng boundNE = new LatLng(route.getJSONObject("bounds").getJSONObject("northeast").getDouble("lat"),
                                        route.getJSONObject("bounds").getJSONObject("northeast").getDouble("lng"));
                                LatLng boundSW = new LatLng(route.getJSONObject("bounds").getJSONObject("southwest").getDouble("lat"),
                                        route.getJSONObject("bounds").getJSONObject("southwest").getDouble("lng"));
                                for(int i=0; i<legs.length(); i++) {
                                    JSONObject leg = legs.getJSONObject(i);
                                    JSONArray steps = leg.getJSONArray("steps");
                                    for(int j=0; j<steps.length(); j++) {
                                        step = steps.getJSONObject(j);
                                        List<LatLng> ltlng = PolyUtil.decode(step.getJSONObject("polyline").getString("points"));
                                        for(int k=0; k<ltlng.size(); k++)
                                            po.add(ltlng.get(k));
                                    }
                                }

                                int comma = from.indexOf(",");
                                String name = "";
                                if(comma == -1) {
                                    name = from;
                                } else {
                                    name = from.substring(0, comma);
                                }

                                fromMarker = map.addMarker(new MarkerOptions().position(new LatLng(legs.getJSONObject(0).
                                        getJSONArray("steps").getJSONObject(0).getJSONObject("start_location").getDouble("lat"),
                                        legs.getJSONObject(0)
                                                .getJSONArray("steps").getJSONObject(0).getJSONObject("start_location").getDouble("lng"))));
                                fromMarker.setTitle(name);
                                fromMarker.showInfoWindow();
                                po.color(Color.BLUE);
                                directions = map.addPolyline(po);
                                map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(boundSW, boundNE), 0));


                            } catch(JSONException exp) {
                                Toast.makeText(ctx, "No routes.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } catch(JSONException exp) {
                        Toast.makeText(ctx, "Directions response is not a JSON.", Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println(error.toString());
                Toast.makeText(ctx, "Directions request error", Toast.LENGTH_SHORT).show();
            }
        });

        api.add(stringRequest);
    }

    public MapFragment() {
        // Required empty public constructor
    }

   /* @Override
    public void geoClicked(String val) {

    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        try {
            mtx.acquire();
        } catch(InterruptedException exp) {

        }

        ctx = getActivity();
        api = Api.getInstance(ctx);

        travelMode = "driving";



        root = inflater.inflate(R.layout.fragment_map, container, false);
        travelModeSpinner = (Spinner)root.findViewById(R.id.travelMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.travel_modes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        travelModeSpinner.setAdapter(adapter);
        travelModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        travelMode = "driving";
                        break;
                    case 1:
                        travelMode = "bicycling";
                        break;
                    case 2:
                        travelMode = "transit";
                        break;
                    case 3:
                        travelMode = "walking";
                        break;
                    default:
                        travelMode = "driving";
                        break;
                }
                updatePath();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                travelMode = "driving";
                updatePath();
            }
        });

        locationBox = (AutoCompleteTextView) root.findViewById(R.id.mapFrom);
        GeoAutocompleteAdapter ac_adapter = new GeoAutocompleteAdapter(getActivity());
        locationBox.setAdapter(ac_adapter);
        locationBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = (String)parent.getItemAtPosition(position);
                updatePath(text);
            }
        });


        if(details != null) {
            putMarker();
        }
        mtx.release();
        return root;
    }

}
