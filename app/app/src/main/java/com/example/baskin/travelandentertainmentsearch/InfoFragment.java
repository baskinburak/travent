package com.example.baskin.travelandentertainmentsearch;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.concurrent.Semaphore;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {


    private JSONObject details;

    private Semaphore mtx = new Semaphore(1, true);

    View root;

    public InfoFragment() {
        // Required empty public constructor
    }

    public void setDetails(JSONObject d) {
        try {
            mtx.acquire();
        } catch(Exception exp) {

        }
        details = d;

        if(root != null) {
            setViews();
        }

        mtx.release();
    }

    private void setViews() {
        // set root using details.

        LinearLayout addrL = (LinearLayout) root.findViewById(R.id.infoAddressLayout);
        LinearLayout pnL = (LinearLayout) root.findViewById(R.id.infoPhoneLayout);
        LinearLayout prL = (LinearLayout) root.findViewById(R.id.infoPriceLayout);
        LinearLayout rtL = (LinearLayout) root.findViewById(R.id.infoRatingLayout);
        LinearLayout googL = (LinearLayout) root.findViewById(R.id.infoGooglePageLayout);
        LinearLayout wsL = (LinearLayout) root.findViewById(R.id.infoWebsiteLayout);

        TextView addr = (TextView) root.findViewById(R.id.infoAddress);
        TextView pn = (TextView) root.findViewById(R.id.infoPhone);
        TextView pr = (TextView) root.findViewById(R.id.infoPrice);
        RatingBar rt = (RatingBar) root.findViewById(R.id.infoRating);
        TextView goog = (TextView) root.findViewById(R.id.infoGooglePage);
        TextView ws = (TextView) root.findViewById(R.id.infoWebsite);
        if(!details.has("formatted_address")) {
            addrL.setVisibility(View.GONE);
        } else {
            addrL.setVisibility(View.VISIBLE);
            try {
                addr.setText(details.getString("formatted_address"));
            } catch(JSONException exp) {

            }
        }

        if(!details.has("international_phone_number")) {
            pnL.setVisibility(View.GONE);
        } else {
            pnL.setVisibility(View.VISIBLE);
            try {
                pn.setText(details.getString("international_phone_number"));
            } catch(JSONException exp) {

            }
        }

        if(!details.has("price_level")) {
            prL.setVisibility(View.GONE);
        } else {
            prL.setVisibility(View.VISIBLE);
            try {
                pr.setText(new String(new char[details.getInt("price_level")]).replace("\0", "$"));
            } catch(JSONException exp) {

            }
        }

        if(!details.has("rating")) {
            rtL.setVisibility(View.GONE);
        } else {
            rtL.setVisibility(View.VISIBLE);
            try {
                rt.setRating((float)details.getDouble("rating"));
            } catch(JSONException exp) {

            }
        }

        if(!details.has("url")) {
            googL.setVisibility(View.GONE);
        } else {
            googL.setVisibility(View.VISIBLE);
            try {
                goog.setText(details.getString("url"));
            } catch(JSONException exp) {

            }
        }

        if(!details.has("website")) {
            wsL.setVisibility(View.GONE);
        } else {
            wsL.setVisibility(View.VISIBLE);
            try {
                ws.setText(details.getString("website"));
            } catch(JSONException exp) {

            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        try {
            mtx.acquire();
        } catch(Exception exp) {

        }


        root = inflater.inflate(R.layout.fragment_info, container, false);

        if(details != null) {
            setViews();
        }

        mtx.release();
        return root;
    }

}
