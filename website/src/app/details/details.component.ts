import { Component, OnInit, Input, ViewChild, ElementRef, ChangeDetectorRef, Output, EventEmitter, ApplicationRef } from '@angular/core';
import { DetailsService } from '../services/details.service';
import { FavoritesBoxService } from '../services/favorites-box.service';
import { SearchFieldsService } from '../services/search-fields.service';
import { CurrentLocationService } from '../services/current-location.service';
import { ApiService } from '../services/api.service';
import { ProgressService } from '../services/progress.service';
import { RatingModule } from 'ngx-rating';
import { } from 'googlemaps';
import * as moment from 'moment';

import { trigger,state,style,transition,animate } from '@angular/animations';

declare var $ :any;

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.css'],
  animations: [
    trigger("fade-in", [
      state('seen', style({
        opacity: 1
      })),
      state('not-seen', style({
        opacity: 0
      })),
      transition('not-seen => seen', animate('500ms ease-in'))
    ])
  ]
})
export class DetailsComponent implements OnInit {
  @Output("go-back") lists_button = new EventEmitter();
  @Input("place") place;
  @ViewChild("mapElement") mapView: ElementRef;
  @ViewChild("mapPanel") mapPanel: ElementRef;
  @ViewChild("mapformFromInput") frominput: ElementRef;
  @ViewChild("streetViewMapElement") streetViewMapView: ElementRef;
  @ViewChild("infoLink") infoButton: ElementRef;
  @ViewChild("twitterLink") twitter_link: ElementRef;

  no_route_error = false;

  fade_state = 'seen';

  map = null;
  streetmap = null;
  mapform = {
    from: '',
    to: '',
    mode: 'DRIVING'
  };

  directionsService = null;
  directionsDisplay = null;

  viewMode = 'map';

  to_marker = null;
  lilmanimage = {
    'map': 'http://cs-server.usc.edu:45678/hw/hw8/images/Pegman.png',
    'street': 'http://cs-server.usc.edu:45678/hw/hw8/images/Map.png'
  };

  reviews = {
    'Google': {
      'default': [],
      'highest_rating': [],
      'lowest_rating': [],
      'most_recent': [],
      'least_recent': []
    },
    'Yelp': {
      'default': [],
      'highest_rating': [],
      'lowest_rating': [],
      'most_recent': [],
      'least_recent': []
    }
  };

  current_reviews = 'Google';
  current_order= 'default';

  current_order_text= {
    'default': 'Default Order',
    'highest_rating': 'Highest Rating',
    'lowest_rating': 'Lowest Rating',
    'most_recent': 'Most Recent',
    'least_recent': 'Least Recent'
  };

  twitter_url = "";

  show_details_fetch_error = false;

  constructor(private detailsService: DetailsService,
              private ref: ChangeDetectorRef,
              private favbox: FavoritesBoxService,
              private searchFields: SearchFieldsService,
              private currentLocService: CurrentLocationService,
              private api: ApiService,
              private progress: ProgressService,
              private appref: ApplicationRef) { }

  animateReviews() {
    this.fade_state = 'not-seen';
    setTimeout(()=>{this.fade_state='seen'});
  }

  toggleViewMode() {
    if(this.viewMode === 'map') {
      var panorama = new google.maps.StreetViewPanorama(this.streetViewMapView.nativeElement, {
        position: new google.maps.LatLng(this.detailsService["details"]["geometry"]["location"].lat(), this.detailsService["details"]["geometry"]["location"].lng()),
        pov: {
          heading: 34,
          pitch: 10
        }
      });
      this.streetmap.setStreetView(panorama);
      this.viewMode='street';
    } else {
      this.viewMode = 'map';
    }
  }

  ngOnInit() {
    let autocomplete = new google.maps.places.Autocomplete(this.frominput.nativeElement, {
      types: ["address"]
    });

    this.directionsService = new google.maps.DirectionsService();
    this.directionsDisplay = new google.maps.DirectionsRenderer();
    let mapOptions = {
      zoom: 17,
      center: {lat: 0, lng: 0}
    };

    this.map = new google.maps.Map(this.mapView.nativeElement, mapOptions);
    this.streetmap = new google.maps.Map(this.streetViewMapView.nativeElement, mapOptions);
    this.directionsDisplay.setMap(this.map);

  }

  getDirections() {
    this.viewMode='map';
    this.mapform.from = this.frominput.nativeElement.value;
    this.directionsDisplay.set('directions', null);
    this.to_marker.setMap(null);
    let from_where = null;
    if(this.mapform.from.trim() === 'Your location' || this.mapform.from.trim().toLowerCase() === 'my location') {
      from_where = new google.maps.LatLng(this.currentLocService.latlon.lat, this.currentLocService.latlon.lon);
    } else {
      from_where = this.mapform.from.trim();
    }


    let to_lat_lon = new google.maps.LatLng(this.detailsService["details"]["geometry"]["location"].lat(), this.detailsService["details"]["geometry"]["location"].lng());
    let directionsReq = {
      'origin': from_where,
      'destination': to_lat_lon,
      'travelMode' : this.mapform.mode,
      'provideRouteAlternatives': true,
      'unitSystem': google.maps.UnitSystem.IMPERIAL
    };

    this.directionsService.route(directionsReq, function(result, status){
      if(status === 'OK') {
        this.directionsDisplay.setDirections(result);
        this.directionsDisplay.setPanel(this.mapPanel.nativeElement);
        this.no_route_error = false;
        this.ref.detectChanges();
      } else {
        this.no_route_error = true;
        this.ref.detectChanges();
      }
    }.bind(this));
  }

  ngOnChanges() {
    this.no_route_error = false;
    this.detailsService.details = {};
    this.detailsService.place = {};
    this.show_details_fetch_error = false;
    if(this.place) {
      this.viewMode='map';
      this.infoButton.nativeElement.click();
      if(this.searchFields.use_current_loc) {
        this.mapform.from = "Your location";
      } else {
        this.mapform.from = this.searchFields.location;
      }
      this.mapform.mode = 'DRIVING';
      let service = new google.maps.places.PlacesService(this.map);
      let request = {
        placeId: this.place.id
      };
      setTimeout(()=>{this.progress.progress = 33;});
      service.getDetails(request, function(details, status) {
        setTimeout(()=>{this.progress.progress = 66; this.appref.tick();});
        if(status!=="OK") {
          this.show_details_fetch_error = true;
          return;
        }
        this.detailsService.place = this.place;
        this.detailsService.details = details;
        let text = "Check out " + details.name + " located at " + details.formatted_address;
        if(details.hasOwnProperty("website")) {
          text +=" Website: " + details.website;
        }
        text +=" #TravelAndEntertainmentSearch";
        this.twitter_url = "https://twitter.com/intent/tweet?text="+encodeURIComponent(text);
        this.mapform.to = details.name + ", " + details.formatted_address;
        if(this.detailsService.details.hasOwnProperty("price_level")) {
          this.detailsService.details.price_level = "$".repeat(this.detailsService.details.price_level);
        }

        if(this.detailsService.details.hasOwnProperty("rating")) {
          this.detailsService.details["max_rating"] = Math.ceil(this.detailsService.details.rating);
        }

        details.hw8_photo_urls = [];
        if(details.photos) {
          details.photos.forEach(function(photo){
            let url = photo.getUrl({'maxWidth': 2000, 'maxHeight': 2000 });
            details.hw8_photo_urls.push(url);
          });
        }

        if(details.hasOwnProperty("opening_hours")) {

          let offset = details["utc_offset"];
          let now = moment().utcOffset(offset);
          details.hw8_current_day = (now.weekday()+6)%7;
          details.hw8_weekday_text = [];
          details.opening_hours.weekday_text.forEach(function(val){
            details.hw8_weekday_text.push({
              hrs: val.substring(val.indexOf(" ") + 1),
              day: val.substring(0, val.indexOf(":"))
            });
          });
          //details.hw8_weekday_text.push(details.hw8.weekday_text.shift());
          for(let i=0; i<details.hw8_current_day; i++) {
            details.hw8_weekday_text.push(details.hw8_weekday_text.shift());
          }
          if(details.opening_hours.open_now) {
            let period_text = details.opening_hours.weekday_text[details.hw8_current_day].substring(details.opening_hours.weekday_text[details.hw8_current_day].indexOf(" ")+1);
            details.hw8_time_text = "Open now: " + period_text;
          } else {
            details.hw8_time_text = "Closed";
          }

        }

        /* set map */
        let latlon = new google.maps.LatLng(details.geometry.location.lat(), details.geometry.location.lng());
        this.directionsDisplay.set('directions', null);
        if(this.to_marker) {
          this.to_marker.setMap(null);
        }
        this.to_marker = new google.maps.Marker({
          position: latlon,
          map: this.map
        });
        this.map.setCenter(latlon);

        this.ref.detectChanges();

        /*
            load reviews
        */
        this.current_reviews = 'Google';
        this.current_order = 'default';
        this.reviews = {
          'Google': {
            'default': [],
            'highest_rating': [],
            'lowest_rating': [],
            'most_recent': [],
            'least_recent': []
          },
          'Yelp': {
            'default': [],
            'highest_rating': [],
            'lowest_rating': [],
            'most_recent': [],
            'least_recent': []
          }
        };

        this.reviews.Google.default = [];
        if(details.hasOwnProperty("reviews")) {
          details.reviews.forEach(function(val){
            let rev = {
              "author_name": val["author_name"],
              "author_image": val["profile_photo_url"],
              "author_url": val["author_url"],
              "time": moment.unix(val["time"]).format("YYYY-MM-DD h:mm:ss"),
              "rating": val["rating"],
              "content": val["text"]
            };
            this.reviews.Google.default.push(rev);
          }.bind(this));
        }
        this.sortGenGoogleReviews();

        /* load yelp reviews */
        let params = {};
        params["name"] = details.name;
        if(details.address_components) {
          details.address_components.forEach(function(value){
            if(value.types.indexOf("locality") > -1) {
              params["city"] = value.long_name;
            } else if(value.types.indexOf("country") > -1) {
              params["country"] = value.short_name;
            } else if(value.types.indexOf("administrative_area_level_1") > -1) {
              params["state"] = value.short_name;
            } else if(value.types.indexOf("postal_code") > -1) {
              if(params["postal_code"]) {
                params["postal_code"] = value.long_name + "-" + params["postal_code"];
              } else {
                params["postal_code"] = value.long_name;
              }
            } else if(value.types.indexOf("postal_code_suffix") > -1) {
              if(params["postal_code"]) {
                params["postal_code"] = params["postal_code"] + "-" + value.long_name;
              } else {
                params["postal_code"] = value.long_name;
              }
            }
          });
        }

        if(details.international_phone_number) {
          params["phone"] = details.international_phone_number.replace(/ /g,'').replace(/\(/g, '').replace(/\)/g, '').replace(/-/g, '');
        }
        params["lat"] = details.geometry.location.lat();
        params["lon"] = details.geometry.location.lng();

        if(details.formatted_address) {
          if(details.formatted_address.length <= 64*3) {
            let components = details.formatted_address.split(",");
            let idx = 0;
            let res = "";
            while(idx < components.length && res.length + components[idx].length < 63) {
              res += components[idx] + ",";
              idx++;
            }
            if(res.length > 0)
              params["address1"] = res.substring(0, res.length-1);
            res = "";

            while(idx<components.length && res.length + components[idx].length < 63) {
              res += components[idx] + ",";
              idx++;
            }

            if(res.length > 0)
              params["address2"] = res.substring(0, res.length-1);
            res = "";

            while(idx<components.length && res.length + components[idx].length < 63) {
              res += components[idx] + ",";
              idx++;
            }
            if(res.length > 0)
              params["address3"] = res.substring(0, res.length-1);
          }
        }

        this.api.loadYelpReviews(params, function(err, result) {
          setTimeout(()=>{this.progress.progress = 99;this.appref.tick(); setTimeout(()=>{this.progress.progress = 100; this.appref.tick();}, 500);});
          if(!err) {
            this.reviews.Yelp.default = result;
          }
          this.sortGenYelpReviews();
          this.ref.detectChanges();
        }.bind(this));

      }.bind(this));
    }

  }

  private compare(a, b) {
    if(a<b) return -1;
    if(a>b) return 1;
    return 0;
  }

  sortGenGoogleReviews() {
    this.reviews.Google.highest_rating = JSON.parse(JSON.stringify(this.reviews.Google.default));
    this.reviews.Google.lowest_rating = JSON.parse(JSON.stringify(this.reviews.Google.default));
    this.reviews.Google.most_recent = JSON.parse(JSON.stringify(this.reviews.Google.default));
    this.reviews.Google.least_recent = JSON.parse(JSON.stringify(this.reviews.Google.default));

    this.reviews.Google.highest_rating.sort(function(b,a){return this.compare(a["rating"],b["rating"]);}.bind(this));
    this.reviews.Google.lowest_rating.sort(function(a,b){return this.compare(a["rating"],b["rating"]);}.bind(this));
    this.reviews.Google.most_recent.sort(function(b,a){return this.compare(a["time"],b["time"]);}.bind(this));
    this.reviews.Google.least_recent.sort(function(a,b){return this.compare(a["time"],b["time"]);}.bind(this));
  }

  sortGenYelpReviews() {
    this.reviews.Yelp.highest_rating = JSON.parse(JSON.stringify(this.reviews.Yelp.default));
    this.reviews.Yelp.lowest_rating = JSON.parse(JSON.stringify(this.reviews.Yelp.default));
    this.reviews.Yelp.most_recent = JSON.parse(JSON.stringify(this.reviews.Yelp.default));
    this.reviews.Yelp.least_recent = JSON.parse(JSON.stringify(this.reviews.Yelp.default));

    this.reviews.Yelp.highest_rating.sort(function(b,a){return this.compare(a["rating"],b["rating"]);}.bind(this));
    this.reviews.Yelp.lowest_rating.sort(function(a,b){return this.compare(a["rating"],b["rating"]);}.bind(this));
    this.reviews.Yelp.most_recent.sort(function(b,a){return this.compare(a["time"],b["time"]);}.bind(this));
    this.reviews.Yelp.least_recent.sort(function(a,b){return this.compare(a["time"],b["time"]);}.bind(this));
  }

  ngAfterViewChecked() {
    this.ref.detectChanges();
  }

}
