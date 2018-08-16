import { Component, OnInit, ElementRef, ViewChild, Output, EventEmitter } from '@angular/core';
import { SearchFields } from '../models/search-fields.model';
import { CurrentLocationService } from '../services/current-location.service';
import { ApiService } from '../services/api.service';

import { SearchFieldsService } from '../services/search-fields.service';
import { ProgressService } from '../services/progress.service';
import { } from 'googlemaps';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {

  @ViewChild("searchForm") search_form: ElementRef;
  @ViewChild("locationElement") location_element: ElementRef;


  form_changed = false;
  keyword_changed = false;
  location_changed = false;

  @Output("searchdone") search_done = new EventEmitter();
  @Output("reset") ireset = new EventEmitter();
  @Output("searcherr") search_err = new EventEmitter();

  @ViewChild("keywordInput") keyword_input: ElementRef;


  constructor(private currentLocService: CurrentLocationService,
              private api: ApiService,
              private search_fields: SearchFieldsService,
              private progress: ProgressService) { }

  ngOnInit() {
    let autocomplete = new google.maps.places.Autocomplete(this.location_element.nativeElement, {
      types: ["address"]
    });
  }


  resetForm() {
    this.search_form.nativeElement.reset();
    this.search_fields.reset();
    this.form_changed = false;
    this.keyword_changed = false;
    this.location_changed = false;
    this.ireset.emit();
  }


  reportValidity() {
    this.search_form.nativeElement.reportValidity();
  }


  searchOK() {
    return this.search_form.nativeElement.checkValidity() && this.currentLocService.latlon.lat != -1 && this.currentLocService.latlon.lon != -1 &&
            this.search_fields.keyword.trim()!=="" && (this.search_fields.use_current_loc || this.search_fields.location.trim() !== "");
  }

  keywordOK() {
    return this.search_fields.keyword.trim() != "";
  }

  locationOK() {
    return this.search_fields.location.trim() != "";
  }

  private searchCallback(err, results) {
    this.progress.progress = 99;
    setTimeout(()=>{this.progress.progress = 100},500);
    if(err) {
      this.search_err.emit();
      return;
    }
    if(results.places.length > 0) {
      this.search_done.emit(results);
    } else {
      this.search_done.emit(null);
    }
  }

  performSearch() {
    this.progress.progress = 0;
    setTimeout(()=>{this.progress.progress = 50},100);
    let params = {};
    params["keyword"] = this.search_fields.keyword;
    params["category"] = this.search_fields.category;
    if(this.search_fields.distance.toString().trim() === "") {
      params["distance"] = 10;
    } else {
      params["distance"] = this.search_fields.distance;
    }

    console.log(params);
    if(this.search_fields.use_current_loc) {
      params["lat"] = this.currentLocService.latlon.lat;
      params["lon"] = this.currentLocService.latlon.lon;
      this.api.searchWithLatLon(params, this.searchCallback.bind(this));
    } else {
      this.search_fields.location = this.location_element.nativeElement.value;
      params["location"] = this.search_fields.location;

      this.api.searchWithLocation(params, this.searchCallback.bind(this));
    }
  }
}
