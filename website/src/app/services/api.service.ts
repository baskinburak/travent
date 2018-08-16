import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { LatLon } from '../models/latlon.model';

@Injectable()
export class ApiService {


  private ip_api_url = "http://ip-api.com/json/";
  private freegeoip_url = "http://freegeoip.net/json/";


//  private hw8apibase = "http://localhost:3000/";
  private hw8apibase = "http://localhost:3000/";
  constructor(private http: HttpClient) { }

  ipApiGetLanLon(callback) {
    this.http.get(this.ip_api_url).subscribe(
      data => {
        if(data.hasOwnProperty("lat") && data.hasOwnProperty("lon")) {
          let latlon: LatLon = new LatLon(parseFloat(data["lat"]), parseFloat(data["lon"]));
          callback(null, latlon);
        } else {
          callback("ip_api_access_error");
        }
      },
      error => {
        callback("http_error");
      }
    );
  }

  freeGeoApiGetLanLon(callback) {
    this.http.get(this.freegeoip_url).subscribe(
      data => {
        if(data.hasOwnProperty("latitude") && data.hasOwnProperty("longitude")) {
          let latlon: LatLon = new LatLon(parseFloat(data["latitude"]), parseFloat(data["longitude"]));
          callback(null, latlon);
        } else {
          callback("freegeoip_access_error")
        }
      },
      error => {
        callback("http_error")
      }
    );
  }

  searchWithLocation(params, callback) {
    if(!params.hasOwnProperty("keyword") ||
       !params.hasOwnProperty("category") ||
       !params.hasOwnProperty("distance") ||
       !params.hasOwnProperty("location")) {
         callback("missing_parameters");
         return;
     }

    let requrl: string = this.hw8apibase + "search/location/"
                    + encodeURIComponent(params["keyword"]) + "/"
                    + encodeURIComponent(params["category"]) + "/"
                    + encodeURIComponent(params["distance"].toString()) + "/"
                    + encodeURIComponent(params["location"]);

    this.http.get(requrl).subscribe(
      data => {
        callback(null, data);
      },
      error => {
        callback("api_error");
      }
    );

  }

  searchWithLatLon(params, callback) {
    if(!params.hasOwnProperty("keyword") ||
       !params.hasOwnProperty("category") ||
       !params.hasOwnProperty("distance") ||
       !params.hasOwnProperty("lat") ||
       !params.hasOwnProperty("lon")) {
         callback("missing_parameters");
         return;
     }


     let requrl: string = this.hw8apibase + "search/latlon/"
                     + encodeURIComponent(params["keyword"]) + "/"
                     + encodeURIComponent(params["category"]) + "/"
                     + encodeURIComponent(params["distance"].toString()) + "/"
                     + encodeURIComponent(params["lat"].toString()) + "/"
                     + encodeURIComponent(params["lon"].toString());


     this.http.get(requrl).subscribe(
       data => {
        callback(null, data);
       },
       error => {
         callback("api_error");
       }
     );
  }

  retrieveNextPage(params, callback) {
    if(!params.hasOwnProperty("next_page_token")) {
      callback("missing_parameters");
      return;
    }

    let requrl: string = this.hw8apibase + "nextpage/"
                    + encodeURIComponent(params["next_page_token"]);

    this.http.get(requrl).subscribe(
      data => {
        callback(null, data);
      },
      error => {
        callback("api_error");
      }
    );
  }

  loadYelpReviews(params, callback) {
    if(!params.hasOwnProperty("name") ||
       !params.hasOwnProperty("city") ||
       !params.hasOwnProperty("state") ||
       !params.hasOwnProperty("country")) {
         callback("missing_parameters");
         return;
       }

     let requrl: string = this.hw8apibase + "yelp?";
     for(let key in params) {
       requrl += encodeURIComponent(key) + "=" + encodeURIComponent(params[key]) + "&";
     }
     requrl = requrl.substring(0, requrl.length-1);

     this.http.get(requrl).subscribe(
       data => {
         callback(null, data);
       },
       error => {
         callback("api_error");
       }
     );
  }


}
