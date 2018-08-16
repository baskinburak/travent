const express = require("express");
const cors = require('cors');
const request = require("request");
const levenshtein = require('fast-levenshtein');
const geolib = require("geolib");

var app = express();

app.use(cors());
app.use(express.static(__dirname+"/public"));

app.get("/search/location/:keyword/:category/:distance/:location", function(req, res, next) {
  res.locals.keyword = req.params.keyword.trim();
  res.locals.category = req.params.category.trim();
  res.locals.distance = req.params.distance.trim();

  var location = req.params.location.trim();
  var geocoding_url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + location + "&key=AIzaSyDbazHfKWJM5PgeSlSzuMnL5-bvrs2B1P4";
  request(geocoding_url, {json: true}, function(err, response, body) {
    if(!(body.hasOwnProperty("results")) ||
       body.results[0] === undefined ||
       !(body.results[0].hasOwnProperty("geometry")) ||
       !(body.results[0].geometry.hasOwnProperty("location")) ||
       !(body.results[0].geometry.location.hasOwnProperty("lat")))
    {
      res.locals.lat = -1;
      res.locals.lon = -1;
    } else {
      res.locals.lat = body.results[0].geometry.location.lat.toString();
      res.locals.lon = body.results[0].geometry.location.lng.toString();
    }
    next();
  });
});

app.get("/search/latlon/:keyword/:category/:distance/:lat/:lon", function(req, res, next) {
  res.locals.keyword = req.params.keyword.trim();
  res.locals.category = req.params.category.trim();
  res.locals.distance = req.params.distance.trim();
  res.locals.lat = req.params.lat.trim();
  res.locals.lon = req.params.lon.trim();
  next();
});

app.get(/^\/search.*/, function(req, res) {
  if(!('keyword' in res.locals) ||
  !('category' in res.locals) ||
  !('distance' in res.locals) ||
  !('lat' in res.locals) ||
  !('lon' in res.locals)) {
    res.send("fail");
    return;
  }

  var keyword = res.locals.keyword;
  var category = res.locals.category;
  var distance = res.locals.distance;
  var lat = res.locals.lat;
  var lon = res.locals.lon;


  var nearbysearch_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?"
                         + "key=AIzaSyAYroygRlN49t4yVBnhrwEFHm40reWutqg"
                         + "&location=" + encodeURIComponent(lat + "," + lon)
                         + "&radius=" + encodeURIComponent((Math.floor(parseFloat(distance) * 1609.34)).toString())
                         + "&keyword=" + encodeURIComponent(keyword);
  if(category !== "default") {
    nearbysearch_url += "&type=" + category
  }



  request(nearbysearch_url, {json: true}, function(err, response, body) {
    var results = {};
    if(body.hasOwnProperty("next_page_token")) {
      results.next_page_token = body.next_page_token;
    }
    results.places = [];
    if(body.hasOwnProperty("results")) {
      body.results.forEach(function(value) {
        var place = {};
        place.icon = value.icon;
        place.id = value.place_id;
        place.lat = value.geometry.location.lat;
        place.lon = value.geometry.location.lng;
        place.address = value.vicinity;
        place.name = value.name;
        results.places.push(place);
      });
    }
    res.json(results);
  });
});

app.get("/nextpage/:npt", function(req, res) {
  var npt = req.params.npt.trim();

  var nextpage_url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=AIzaSyAYroygRlN49t4yVBnhrwEFHm40reWutqg"
                    + "&pagetoken=" + encodeURIComponent(npt);

  var nextpagereq = function() {
    request(nextpage_url, {json: true}, function(err, response, body){
      if(!body.hasOwnProperty("results") || body.results.length === 0) {
        setTimeout(nextpagereq, 100);
        return;
      }
      var results = {};
      if(body.hasOwnProperty("next_page_token")) {
        results.next_page_token = body.next_page_token;
      }
      results.places = [];
      if(body.hasOwnProperty("results")) {
        body.results.forEach(function(value) {
          var place = {};
          place.icon = value.icon;
          place.id = value.place_id;
          place.lat = value.geometry.location.lat;
          place.lon = value.geometry.location.lng;
          place.address = value.vicinity;
          place.name = value.name;
          results.places.push(place);
        });
      }
      this.json(results);
    }.bind(this));
  }.bind(res);

  nextpagereq();

});

function sameplace(yelpbusiness, queryvars) {
  if(yelpbusiness.hasOwnProperty("coordinates")) {
    var distance =geolib.getDistance({
      latitude: queryvars.lat,
      longitude: queryvars.lon
    }, {
      latitude: yelpbusiness.coordinates.latitude,
      longitude: yelpbusiness.coordinates.longitude
    });
    if(distance < 40) {
      return true;
    }
    if(distance > 100) {
      return false;
    }
  }

  if(queryvars.hasOwnProperty("phone") && yelpbusiness.hasOwnProperty("phone")) {
    if(queryvars.phone === yelpbusiness.phone) {
      return true;
    } else {
      return false;
    }
  }


  if(queryvars.name.indexOf(yelpbusiness.name) !== -1) {
    return true;
  }

  if(yelpbusiness.name.indexOf(queryvars.name) !== -1) {
    return true;
  }

  var levdist = levenshtein.get(queryvars.name, yelpbusiness.name) / Math.max(queryvars.name.length, yelpbusiness.name.length);
  if(levdist < 0.2) {
    return true;
  }
  if(levdist > 0.5) {
    return false;
  }

  return false;
}

app.get("/yelp", function(req, res){
  /*
    name: req
    city: req
    country: req
    state: req
    postal_code
    phone
    lat
    lon
    address1
    address2
    address3

  */
  if(!req.query.hasOwnProperty("name") ||
     !req.query.hasOwnProperty("city") ||
     !req.query.hasOwnProperty("country") ||
     !req.query.hasOwnProperty("state")) {
    res.send("fail");
    return;
  }


  var yelp_bestmatch_url = "https://api.yelp.com/v3/businesses/matches/best?";
  for(var key in req.query) {
    yelp_bestmatch_url += encodeURIComponent(key) + "=" + encodeURIComponent(req.query[key]) + "&";
  }
  yelp_bestmatch_url = yelp_bestmatch_url.substring(0, yelp_bestmatch_url.length - 1);

  request({
    headers: {
      'Authorization': 'Bearer JrZro2HyndrHvGcpxQkZ7OICrb339ed53Lkr3AKVGCBDAK2XTNW1s_VQsPeJ8LoxWMp2F40CHIVWa2HW1CHuJ4BWIZZh5DJ_aOORj9-0qhL92hvMNhXY39W_8gTEWnYx'
    },
    url: yelp_bestmatch_url,
    method: 'GET',
    json: true
    }, function(err, response, body) {
    if(err) {
      res.json([]);
    } else {
      if(body.businesses.length > 0) {
        var business = body.businesses[0];
        if(sameplace(business, req.query)) {
          var yelpid = business.id;
          var requrl = "https://api.yelp.com/v3/businesses/"+yelpid+"/reviews";
          request({
            headers: {
            'Authorization': 'Bearer JrZro2HyndrHvGcpxQkZ7OICrb339ed53Lkr3AKVGCBDAK2XTNW1s_VQsPeJ8LoxWMp2F40CHIVWa2HW1CHuJ4BWIZZh5DJ_aOORj9-0qhL92hvMNhXY39W_8gTEWnYx'
            },
            url: requrl,
            method: 'GET',
            json: true
          }, function(err, response, body){
            if(err) {
              res.json([]);
            } else {
              if(body.hasOwnProperty("reviews")) {
                var reviews = [];
                body.reviews.forEach(function(val){
                  var rev = {
                    "author_url": val["url"],
                    "author_name": val["user"]["name"],
                    "author_image": val["user"]["image_url"],
                    "time": val["time_created"],
                    "rating": val["rating"],
                    "content": val["text"]
                  };
                  reviews.push(rev);
                });
                res.json(reviews);
              } else {
                res.json([]);
              }
            }
          });
        } else {
          res.json([]);
        }
      } else {
        res.json([]);
      }
    }
  });

});


app.listen(process.env.PORT || 3000);
