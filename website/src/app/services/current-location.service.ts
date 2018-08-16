import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { ApiService } from './api.service';
import { LatLon } from '../models/latlon.model';

@Injectable()
export class CurrentLocationService {

  latlon: LatLon = new LatLon(-1, -1);


  constructor(private api: ApiService) {
    this.api.ipApiGetLanLon(
      function(err, latlon: LatLon) {
        if(err) {
          this.api.freeGeoApiGetLanLon(
            function(err, latlon: LatLon) {
              if(err) {
                this.latlon = new LatLon(34.0266, -118.2831);
              } else {
                this.latlon = latlon;
              }
            }.bind(this)
          );
        } else {
          this.latlon = latlon;
        }
      }.bind(this)
    );
  }

}
