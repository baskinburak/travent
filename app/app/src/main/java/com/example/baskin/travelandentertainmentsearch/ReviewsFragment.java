package com.example.baskin.travelandentertainmentsearch;


import android.content.Intent;
import android.media.Rating;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Semaphore;


/**
 * A simple {@link Fragment} subclass.
 */

public class ReviewsFragment extends Fragment {

    private Spinner orderSpinner;
    private Spinner typeSpinner;
    private LinearLayout noReviews;
    private RecyclerView rv;
    View root;

    private class Review {
        public String author_url = "";
        public String author_name = "";
        public String author_image = "";
        public String time = "";
        public int rating = -1;
        public String content = "";
        public int order;

        @Override
        public String toString() {
            return author_url + ","
                    +author_name + ","
                    +author_image + ","
                    +time + ","
                    +Integer.toString(rating) + ","
                    +content;
        }
    }

    private class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {
        private ArrayList<Review> reviews;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout layout;
            public TextView author_name;
            public TextView content;
            public TextView time;
            public RatingBar rating;
            public ImageView image;
            public ViewHolder(LinearLayout l) {
                super(l);
                layout = l;
                author_name = (TextView) layout.findViewById(R.id.reviewAuthorName);
                content = (TextView) layout.findViewById(R.id.reviewContent);
                time = (TextView) layout.findViewById(R.id.reviewTime);
                rating = (RatingBar) layout.findViewById(R.id.reviewRating);
                image = (ImageView) layout.findViewById(R.id.reviewAuthorImage);
            }
        }

        public ReviewAdapter(ArrayList<Review> r) {
            reviews = r;
        }

        @Override
        public ReviewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int ViewType) {
            LinearLayout l = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.review_item, parent, false);

            ViewHolder vh = new ViewHolder(l);

            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final Review rev = reviews.get(position);

            if(!rev.author_image.equals("")) {
                Picasso.get().load(rev.author_image).into(holder.image);
            } else {
                holder.image.setImageBitmap(null);
            }

            if(!rev.author_name.equals("")) {
                holder.author_name.setVisibility(View.VISIBLE);
                holder.author_name.setText(rev.author_name);
            } else {
                holder.author_name.setVisibility(View.GONE);
            }

            if(!rev.content.equals("")) {
                holder.content.setVisibility(View.VISIBLE);
                holder.content.setText(rev.content);
            } else {
                holder.content.setVisibility(View.GONE);
            }

            if(!rev.time.equals("")) {
                holder.time.setVisibility(View.VISIBLE);
                holder.time.setText(rev.time);
            } else {
                holder.time.setVisibility(View.GONE);
            }

            if(rev.rating != -1) {
                holder.rating.setVisibility(View.VISIBLE);
                holder.rating.setRating(rev.rating);
            } else {
                holder.rating.setVisibility(View.GONE);
            }

            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(rev.author_url));
                    startActivity(browserIntent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return reviews.size();
        }

        public void replace(ArrayList<Review> r) {
            reviews = r;
            notifyDataSetChanged();
        }
    }

    private Semaphore mtx = new Semaphore(1, true);

    private JSONObject details;
    private JSONArray yelpReviews;

    private ArrayList<Review> googleReviewsArr;
    private ArrayList<Review> yelpReviewsArr;

    private ReviewAdapter adapter;

    private String type;
    private String order;

    public void setDetails(JSONObject d) {
        try {
            mtx.acquire();
        } catch(InterruptedException exp) {

        }
        details = d;
        if(googleReviewsArr == null) {
            googleReviewsArr = new ArrayList<Review>();
        }
        googleReviewsArr.clear();
        if(details.has("reviews")) {
            JSONArray reviews = new JSONArray();
            try {
                reviews = details.getJSONArray("reviews");
            } catch(JSONException exp) {

            }


            for(int i=0; i<reviews.length(); i++) {
                try {
                    Review rev = new Review();
                    JSONObject revJ = reviews.getJSONObject(i);
                    rev.order = i;
                    if(revJ.has("author_name")) {
                        rev.author_name = revJ.getString("author_name");
                    }
                    if(revJ.has("author_url")) {
                        rev.author_url = revJ.getString("author_url");
                    }

                    if(revJ.has("profile_photo_url")) {
                        rev.author_image = revJ.getString("profile_photo_url");
                    }

                    if(revJ.has("rating")) {
                        rev.rating = revJ.getInt("rating");
                    }

                    if(revJ.has("time")) {
                        long time = revJ.getLong("time");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        rev.time = sdf.format(new java.util.Date(time*1000));
                    }

                    if(revJ.has("text")) {
                        rev.content = revJ.getString("text");
                    }
                    googleReviewsArr.add(rev);
                } catch(JSONException exp) {

                }
            }
        }
        if(root != null) {
            sortArrays(order);
            if(type.equals("google")) {
                adapter.replace(googleReviewsArr);
            } else {
                adapter.replace(yelpReviewsArr);
            }
        }
        mtx.release();
    }

    public void setYelpReviews(JSONArray y) {
        try {
            mtx.acquire();
        } catch(InterruptedException exp) {

        }
        yelpReviews = y;

        if(yelpReviewsArr == null) {
            yelpReviewsArr = new ArrayList<Review>();
        }
        yelpReviewsArr.clear();

        for(int i=0; i<yelpReviews.length(); i++) {
            Review rev = new Review();
            JSONObject revJ = new JSONObject();
            rev.order = i;
            try {
                revJ = yelpReviews.getJSONObject(i);
            } catch(JSONException exp) {

            }
            try {
                if (revJ.has("author_name")) {
                    rev.author_name = revJ.getString("author_name");
                }
                if (revJ.has("author_url")) {
                    rev.author_url = revJ.getString("author_url");
                }

                if (revJ.has("author_image")) {
                    rev.author_image = revJ.getString("author_image");
                }

                if (revJ.has("rating")) {
                    rev.rating = revJ.getInt("rating");
                }

                if (revJ.has("time")) {
                    rev.time = revJ.getString("time");
                }

                if (revJ.has("content")) {
                    rev.content = revJ.getString("content");
                }
                yelpReviewsArr.add(rev);
            } catch(JSONException exp) {

            }
        }


        if(root != null) {
            showCorrectReviews();
        }

        mtx.release();
    }

    private void showCorrectReviews() {
        sortArrays(order);
        if(type.equals("google")) {
            adapter.replace(googleReviewsArr);
            if(googleReviewsArr.size() == 0) {
                noReviews.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                noReviews.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        } else {
            adapter.replace(yelpReviewsArr);
            if(yelpReviewsArr.size() == 0) {
                noReviews.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                noReviews.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
            }
        }
    }

    public ReviewsFragment() {
        // Required empty public constructor
    }

    private void sortArrays(String order) {
        if(order.equals("default")) {
            Collections.sort(googleReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.order - o2.order;
                }
            });
            Collections.sort(yelpReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.order - o2.order;
                }
            });
        } else if(order.equals("lowest_rating")) {
            Collections.sort(googleReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.rating - o2.rating;
                }
            });
            Collections.sort(yelpReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.rating - o2.rating;
                }
            });

        } else if(order.equals("highest_rating")) {
            Collections.sort(googleReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o2.rating - o1.rating;
                }
            });
            Collections.sort(yelpReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o2.rating - o1.rating;
                }
            });
        } else if(order.equals("least_recent")) {
            Collections.sort(googleReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.time.compareTo(o2.time);
                }
            });
            Collections.sort(yelpReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o1.time.compareTo(o2.time);
                }
            });
        } else if(order.equals("most_recent")) {
            Collections.sort(googleReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o2.time.compareTo(o1.time);
                }
            });
            Collections.sort(yelpReviewsArr, new Comparator<Review>() {
                @Override
                public int compare(Review o1, Review o2) {
                    return o2.time.compareTo(o1.time);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            mtx.acquire();
        } catch(InterruptedException exp) {

        }
        root = (View) inflater.inflate(R.layout.fragment_reviews, container, false);

        noReviews = (LinearLayout) root.findViewById(R.id.reviewsNoReviews);

        type = "google";
        order = "default";

        if(googleReviewsArr == null) {
            googleReviewsArr = new ArrayList<Review>();
        }

        if(yelpReviewsArr == null) {
            yelpReviewsArr = new ArrayList<Review>();
        }

        rv = (RecyclerView) root.findViewById(R.id.reviewsRecycler);
        rv.setHasFixedSize(true);

        RecyclerView.LayoutManager mgr = new LinearLayoutManager(getActivity());
        rv.setLayoutManager(mgr);

        sortArrays(order);


        adapter = new ReviewAdapter(new ArrayList<Review>());

        rv.setAdapter(adapter);


        orderSpinner = (Spinner)root.findViewById(R.id.reviewOrderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getActivity(), R.array.review_orders, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        orderSpinner.setAdapter(adapter);
        orderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        order = "default";
                        break;
                    case 1:
                        order = "highest_rating";
                        break;
                    case 2:
                        order = "lowest_rating";
                        break;
                    case 3:
                        order = "most_recent";
                        break;
                    case 4:
                        order = "least_recent";
                        break;
                    default:
                        order = "default";
                }
                showCorrectReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                order = "default";
                showCorrectReviews();
            }
        });

        typeSpinner = (Spinner)root.findViewById(R.id.reviewTypeSpinner);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this.getActivity(), R.array.review_types, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(adapter2);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        type = "google";
                        break;
                    case 1:
                        type = "yelp";
                        break;
                    default:
                        type = "google";
                }
                showCorrectReviews();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                type="google";
                showCorrectReviews();
            }
        });



        showCorrectReviews();

        mtx.release();
        return root;
    }

}
