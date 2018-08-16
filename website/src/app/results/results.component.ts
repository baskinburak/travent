import { Component, OnInit, Input, EventEmitter, Output } from '@angular/core';
import { FavoritesBoxService } from '../services/favorites-box.service';
import { ApiService } from '../services/api.service';
import { DetailsService } from '../services/details.service';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit {

  @Input("searchresults") search_results;

  @Output("details-clicked") details_clicked = new EventEmitter();

  places_to_show;

  pagination = {
    page: 0,
    next_page_token: null,
    pages: []
  };

  loaded_details = null;


  constructor(private favbox: FavoritesBoxService,
              private api: ApiService,
              private details: DetailsService) { }

  ngOnInit() {
  }

  load_details(place) {
    this.details_clicked.emit(place);
  }

  showPage(pagenum) {
    if(this.pagination.pages.length === pagenum) {
      let params = {};
      params["next_page_token"] = this.pagination.next_page_token;
      this.api.retrieveNextPage(params, (err, data) => {
        if(!err) {
          if(data.hasOwnProperty("next_page_token")) {
              this.pagination.next_page_token = data.next_page_token;
          } else {
            this.pagination.next_page_token = null;
          }
          this.pagination.pages.push(data.places);
          this.places_to_show = this.pagination.pages[pagenum];
          this.pagination.page = pagenum;
        }
      });
    } else {
      this.places_to_show = this.pagination.pages[pagenum];
      this.pagination.page = pagenum;
    }
  }

  ngOnChanges() {
    this.pagination.page = 0;
    this.pagination.pages = [];
    if(this.search_results.hasOwnProperty("next_page_token")) {
      this.pagination.next_page_token = this.search_results.next_page_token;
    } else {
      this.pagination.next_page_token = null;
    }
    if(this.search_results.places.length > 0) {
      this.pagination.pages.push(this.search_results.places);
      this.showPage(0);
    } else {
      // no results
    }

  }

  prevpage() {
    this.showPage(this.pagination.page-1);
  }

  nextpage() {
    this.showPage(this.pagination.page+1);
  }

}
