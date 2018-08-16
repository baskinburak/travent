package com.example.baskin.travelandentertainmentsearch;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

public class Api {
    private static Api apiHandle;
    private RequestQueue reqQueue;
    private static Context ctx;
    private static String hw9apibase = "http://baskincsci571-3ppx9-env.us-west-1.elasticbeanstalk.com/";

    private Api(Context context) {
        ctx = context.getApplicationContext();
        reqQueue = Volley.newRequestQueue(ctx);
    }

    public static synchronized Api getInstance(Context ctx) {
        if(apiHandle == null) {
            apiHandle = new Api(ctx);
        }
        return apiHandle;
    }


    public <T> void add(Request<T> req) {
        reqQueue.add(req);
    }

    public String buildSearchReqWithLatLon(String keyword, String category, String distance, double lat, double lon) {
        String req = "";
        try {
            req = hw9apibase + "search/latlon/"
                    + URLEncoder.encode(keyword, "UTF-8") + "/"
                    + URLEncoder.encode(category, "UTF-8") + "/"
                    + URLEncoder.encode(distance, "UTF-8") + "/"
                    + URLEncoder.encode(Double.toString(lat), "UTF-8") + "/"
                    + URLEncoder.encode(Double.toString(lon), "UTF-8");
        } catch(java.io.UnsupportedEncodingException exp) {

        }
        return req;
    }

    public String buildSearchReqWithLocation(String keyword, String category, String distance, String location) {
        String req = "";
        try {
            req = hw9apibase + "search/location/"
                    + URLEncoder.encode(keyword, "UTF-8") + "/"
                    + URLEncoder.encode(category, "UTF-8") + "/"
                    + URLEncoder.encode(distance, "UTF-8") + "/"
                    + URLEncoder.encode(location, "UTF-8");
        } catch(java.io.UnsupportedEncodingException exp) {

        }
        return req;
    }

    public String buildNPTReq(String npt) {
        String req = "";
        try {
            req = hw9apibase + "nextpage/"
                    + URLEncoder.encode(npt, "UTF-8");
        } catch(java.io.UnsupportedEncodingException exp) {

        }

        return req;
    }

    public String buildPlaceDetailsReq(String id) {
        //String req = "https://maps.googleapis.com/maps/api/place/details/json?key=" + API_KEY + "&placeid=";
        String req = hw9apibase + "details/";
        try {
            req += URLEncoder.encode(id, "UTF-8");
        } catch(java.io.UnsupportedEncodingException exp) {

        }
        return req;
    }

    private boolean JSONArrayHasString(JSONArray arr, String val) {
        for(int i=0; i<arr.length(); i++) {
            try {
                if (arr.getString(i).equals(val)) {
                    return true;
                }
            } catch(JSONException exp) {

            }
        }

        return false;
    }

    public String buildDirectionsReq(String from, double toLat, double toLon, String travelMode) {
        String req = "";
        try {
           req = "https://maps.googleapis.com/maps/api/directions/json?key=AIzaSyA1MrACoQniyw1P0lFWqHuHOeFAURB6gX8"
                    + "&origin=" + URLEncoder.encode(from, "UTF-8")
                    + "&destination=" + Double.toString(toLat) + "," + Double.toString(toLon)
                    + "&mode=" + travelMode;
        } catch(UnsupportedEncodingException exp) {

        }

        return req;
    }

    public String buildYelpReviewsReq(JSONObject details) {
        JSONObject extracted = new JSONObject();

        try {
            extracted.put("name", details.getString("name"));
        } catch(JSONException exp) {

        }

        if(details.has("address_components")) {
            JSONArray address_components = new JSONArray();
            try {
                address_components = details.getJSONArray("address_components");
                for(int i=0; i<address_components.length(); i++) {
                    JSONObject comp = address_components.getJSONObject(i);
                    JSONArray types = comp.getJSONArray("types");
                    if(JSONArrayHasString(types, "locality")) {
                        extracted.put("city", comp.getString("long_name"));
                    } else if(JSONArrayHasString(types, "country")) {
                        extracted.put("country", comp.getString("short_name"));
                    } else if(JSONArrayHasString(types, "administrative_area_level_1")) {
                        extracted.put("state", comp.getString("short_name"));
                    } else if(JSONArrayHasString(types, "postal_code")) {
                        if(extracted.has("postal_code")) {
                            extracted.put("postal_code", comp.getString("long_name") + "-"+extracted.getString("postal_code"));
                        } else {
                            extracted.put("postal_code", comp.getString("long_name"));
                        }
                    } else if(JSONArrayHasString(types, "postal_code_suffix")) {
                        if(extracted.has("postal_code")) {
                            extracted.put("postal_code", extracted.get("postal_code") + "-" + comp.getString("long_name"));
                        } else {
                            extracted.put("postal_code", comp.getString("long_name"));
                        }
                    }
                }
            } catch(JSONException exp) {

            }
        }

        if(extracted.has("state") && !extracted.has("city")) {
            try {
                extracted.put("city", extracted.get("state"));
            } catch(JSONException exp) {

            }
        }

        if(extracted.has("city") && !extracted.has("state")) {
            try {
                extracted.put("state", extracted.get("city"));
            } catch(JSONException exp) {

            }
        }

        if(details.has("international_phone_number")) {
            try {
                extracted.put("phone", details.getString("international_phone_number").replaceAll("[() -]", ""));
            } catch(JSONException exp) {

            }
        }

        try {
            extracted.put("lat", Double.toString(details.getJSONObject("geometry").getJSONObject("location").getDouble("lat")));
            extracted.put("lon", Double.toString(details.getJSONObject("geometry").getJSONObject("location").getDouble("lng")));
        } catch(JSONException exp) {

        }

        try {
            if (details.has("formatted_address")) {
                String address = details.getString("formatted_address");
                if (address.length() <= 64 * 3) {
                    String[] components = address.split(",");
                    int idx = 0;
                    String res = "";

                    while (idx < components.length && res.length() + components[idx].length() < 63) {
                        res += components[idx] + ",";
                        idx++;
                    }

                    if (res.length() > 0) {
                        try {
                            extracted.put("address1", res.substring(0, res.length() - 1));
                        } catch (JSONException exp) {

                        }
                    }
                    res = "";

                    while (idx < components.length && res.length() + components[idx].length() < 63) {
                        res += components[idx] + ",";
                        idx++;
                    }

                    if (res.length() > 0) {
                        try {
                            extracted.put("address2", res.substring(0, res.length() - 1));
                        } catch (JSONException exp) {

                        }
                    }

                    res = "";

                    while (idx < components.length && res.length() + components[idx].length() < 63) {
                        res += components[idx] + ",";
                        idx++;
                    }

                    if (res.length() > 0) {
                        try {
                            extracted.put("address3", res.substring(0, res.length() - 1));
                        } catch (JSONException exp) {

                        }
                    }
                }
            }
        } catch(JSONException exp) {

        }


        String req = hw9apibase + "yelp?";
        Iterator<String> keys = extracted.keys();

        while(keys.hasNext()) {
            try {
                String key = (String) keys.next();
                String value = extracted.getString(key);
                req += key + "=" + URLEncoder.encode(value, "UTF-8") + "&";
            } catch(JSONException exp) {

            } catch(java.io.UnsupportedEncodingException exp) {

            }
        }

        return req.substring(0, req.length() -1);
   }
}
