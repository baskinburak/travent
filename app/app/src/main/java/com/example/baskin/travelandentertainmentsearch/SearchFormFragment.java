package com.example.baskin.travelandentertainmentsearch;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;
import java.util.concurrent.Semaphore;


public class SearchFormFragment extends Fragment implements LocationListener {

    private final int ACCESS_FINE_LOCATION_PERM_ID = 1;

    private String[] category_codes;


    private boolean from_current_location = true;
    private String selectedCategory = "default";

    AutoCompleteTextView locationBox;

    EditText keywordField;
    EditText distanceField;
    EditText locationField;

    TextView locationErrorField;
    TextView keywordErrorField;

    RadioGroup rg;

    Spinner dropdown;

    Context ctx;

    private Api api;

    private FusedLocationProviderClient locationProvider;


    @Override
    public void onLocationChanged(Location location) {

    }

    private class CategorySelectResponder implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            selectedCategory = category_codes[pos];
        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public SearchFormFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ctx = (Context) getActivity();

        api = Api.getInstance(ctx);

        selectedCategory = "default";
        from_current_location = true;

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_search_form, container, false);

        dropdown = (Spinner)v.findViewById(R.id.categorySpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.categories, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new CategorySelectResponder());

        category_codes = getResources().getStringArray(R.array.category_codes);


        rg = (RadioGroup) v.findViewById(R.id.from_radio_group);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                from_current_location = checkedId == R.id.from_current;
                if(!from_current_location) {
                    locationBox.setEnabled(true);
                } else {
                    locationBox.setEnabled(false);
                    locationErrorField.setVisibility(View.GONE);

                }
            }
        });

        locationBox = (AutoCompleteTextView) v.findViewById(R.id.locationInput);
        GeoAutocompleteAdapter ac_adapter = new GeoAutocompleteAdapter(getActivity());
        locationBox.setAdapter(ac_adapter);
        locationBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = (String)parent.getItemAtPosition(position);
            }
        });

        keywordField = (EditText)v.findViewById(R.id.keywordInput);
        distanceField = (EditText)v.findViewById(R.id.distanceInput);
        locationField = (EditText)v.findViewById(R.id.locationInput);
        locationErrorField = (TextView)v.findViewById(R.id.location_error);
        keywordErrorField = (TextView)v.findViewById(R.id.keyword_error);

        Button clearButton = (Button) v.findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                clearForm(v);
            }
        });

        Button searchButton = (Button) v.findViewById(R.id.search_button);
        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doSearch(v);
            }
        });

        locationProvider = LocationServices.getFusedLocationProviderClient(ctx);


        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locreq = new LocationRequest();
            locreq.setNumUpdates(1);
            locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locreq.setInterval(1*1000).setFastestInterval(1*1000);
            locationProvider.requestLocationUpdates(locreq, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locres) {
                    if(locres == null) {
                        return;
                    }
                }
            }, null);
        }


        return v;
    }

    private boolean keywordError() {
        String keyword = keywordField.getText().toString().trim();
        if(keyword.equals("")) {
            keywordErrorField.setVisibility(View.VISIBLE);
            return true;
        }
        keywordErrorField.setVisibility(View.GONE);
        return false;

    }

    private boolean locationError() {
        if(from_current_location) {
            locationErrorField.setVisibility(View.GONE);
            return false;
        }

        String location = locationField.getText().toString().trim();
        if(location.equals("")) {
            locationErrorField.setVisibility(View.VISIBLE);
            return true;
        }
        locationErrorField.setVisibility(View.GONE);
        return false;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == ACCESS_FINE_LOCATION_PERM_ID) {
            if(permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LocationRequest locreq = new LocationRequest();
                locreq.setNumUpdates(1);
                locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                locreq.setInterval(1*1000).setFastestInterval(1*1000);
                try {
                    locationProvider.requestLocationUpdates(locreq, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locres) {
                            if (locres == null) {
                                return;
                            } else {
                                doSearch(null);
                            }
                        }
                    }, null);
                } catch(SecurityException exp) {

                }
            }
        }
    }

    public void doSearch(View view) {
        boolean kwErr = keywordError();
        boolean locErr = locationError();
        if(kwErr || locErr) {
            Toast.makeText(ctx, "Please fix all fields with errors", Toast.LENGTH_LONG).show();
        } else {
            final String keyword = keywordField.getText().toString().trim();
            String dist = distanceField.getText().toString().trim();
            if(dist.equals("")) {
                dist = "10";
            }
            final String distance = dist;
            if(from_current_location) {
                final Location loc = new Location("");



                if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_PERM_ID);
                } else {

                    try {
                        locationProvider.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location != null) {
                                    String reqUrl = api.buildSearchReqWithLatLon(keyword, selectedCategory,
                                            distance, location.getLatitude(), location.getLongitude());
                                    fetchResults(reqUrl);
                                } else {
                                    LocationRequest locreq = new LocationRequest();
                                    locreq.setNumUpdates(1);
                                    locreq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                    locreq.setInterval(1*1000).setFastestInterval(1*1000);
                                    try {
                                        locationProvider.requestLocationUpdates(locreq, new LocationCallback() {
                                            @Override
                                            public void onLocationResult(LocationResult locres) {
                                                if (locres == null) {
                                                    return;
                                                } else {
                                                    doSearch(null);
                                                }
                                            }
                                        }, null);
                                    } catch(SecurityException exp) {

                                    }
                                    Toast.makeText(getActivity(), "No latest location", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (SecurityException e) {

                    }
                }




            } else {
                String location = locationField.getText().toString().trim();
                String reqUrl = api.buildSearchReqWithLocation(keyword, selectedCategory, distance, location);
                fetchResults(reqUrl);
            }
        }
    }

    private void fetchResults(String requestUrl) {
        final ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Fetching results");
        pd.show();
        pd.setCancelable(false);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        pd.dismiss();
                        try {
                            JSONObject resp = new JSONObject(response);
                            Intent ii = new Intent(getActivity(), ResultsActivity.class);
                            ii.putExtra("results", response);
                            startActivity(ii);
                        } catch(JSONException exp) {
                            // not a json.
                            Toast.makeText(getActivity(), "Response is not a JSON.", Toast.LENGTH_SHORT).show();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(getActivity(), "Request error.", Toast.LENGTH_SHORT).show();
                        System.out.println(error.toString());
                    }
                });
        api.add(stringRequest);
    }

    public void clearForm(View view) {
        locationErrorField.setVisibility(View.GONE);
        keywordErrorField.setVisibility(View.GONE);
        keywordField.setText("");
        locationField.setText("");
        distanceField.setText("");
        dropdown.setSelection(0);
        rg.check(R.id.from_current);
    }

}
