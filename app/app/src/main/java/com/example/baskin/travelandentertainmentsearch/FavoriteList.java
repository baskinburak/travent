package com.example.baskin.travelandentertainmentsearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FavoriteList {
    private static FavoriteList favList;
    private Context context;
    private ArrayList<JSONObject> places;
    private File file;

    SharedPreferences spref;
    SharedPreferences.Editor sprefEditor;


    public FavoriteList(Context ctx) {
        context = ctx.getApplicationContext();


        spref = context.getSharedPreferences( context.getString(R.string.spref), context.MODE_PRIVATE);
        sprefEditor = spref.edit();

        String favorites = spref.getString("fav_list", "");




        if(favorites != "") {
            try {
                JSONArray plcs = new JSONArray(favorites);
                places = new ArrayList<JSONObject>();
                for(int i=0; i<plcs.length(); i++) {
                    places.add(plcs.getJSONObject(i));
                }
            } catch(JSONException exp) {
                places = new ArrayList<JSONObject>();
                save();
            }
        } else {
            places = new ArrayList<JSONObject>();
            save();
        }
    }

    public void save() {
        JSONArray arr = new JSONArray();
        for(int i=0; i<places.size(); i++) {
            arr.put(places.get(i));
        }
        sprefEditor.putString("fav_list", arr.toString());
        sprefEditor.commit();
    }

    public void remove(String id) {
        for(int i=0; i<places.size(); i++) {
            JSONObject obj = places.get(i);
            String cid;
            try {
                cid = obj.getString("id");
            } catch(JSONException exp) {
                continue;
            }

            if(cid.equals(id)) {
                places.remove(i);
            }
        }

        save();
    }

    public boolean exists(String id) {
        for(int i=0; i<places.size(); i++) {
            JSONObject cur = places.get(i);
            String cid;
            try {
                cid = cur.getString("id");
            } catch(JSONException exp) {
                continue;
            }
            if(cid.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public void add(JSONObject obj) {
        String id;

        try {
            id = obj.getString("id");
        } catch(JSONException exp) {
            return;
        }

        if(!exists(id)) {
            places.add(obj);
            save();
        }
    }

    public static synchronized FavoriteList getInstance(Context ctx) {
        if(favList == null)
            favList = new FavoriteList(ctx);
        return favList;
    }

    public ArrayList<ArrayList<JSONObject>> paginate() {
        ArrayList<ArrayList<JSONObject>> pages = new ArrayList<ArrayList<JSONObject>>();

        for(int i=0; i<places.size(); i+=20) {
            pages.add(new ArrayList<JSONObject>(
                        places.subList(
                                i, Math.min(i+20, places.size())
                        )
                    )
            );
        }

        return pages;
    }

    public void toggle(JSONObject obj, Context ctx) {
        String id;
        String name;

        try {
            id = obj.getString("id");
            name = obj.getString("name");
        } catch(JSONException exp) {
            return;
        }

        if(exists(id)) {
            remove(id);
            Toast.makeText(ctx, name + " was removed from favorites.", Toast.LENGTH_SHORT).show();

        } else {
            add(obj);
            Toast.makeText(ctx, name + " was added to favorites.", Toast.LENGTH_SHORT).show();
        }
    }

    public int size() {
        return places.size();
    }
}
