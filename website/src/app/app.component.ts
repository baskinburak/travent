import { Component, ViewChild,ElementRef } from '@angular/core';
import { DetailsService } from './services/details.service';
import { ProgressService } from './services/progress.service';
import { FavoritesBoxService } from './services/favorites-box.service';
import { trigger,state,style,transition,animate } from '@angular/animations';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [
    trigger("slide-from-left", [
      state('middle', style({
        position: 'relative',
        left: 0
      })),
      state('left', style({
        position: 'relative',
        left: "-"+window.screen.width+"px",
      })),
      transition('left => middle', animate('500ms ease'))
    ]),
    trigger("slide-from-right", [
      state('middle', style({
        position: 'relative',
        left: 0
      })),
      state('left', style({
        position: 'relative',
        left: window.screen.width+"px",
      })),
      transition('left => middle', animate('500ms ease'))
    ]),
  ]
})
export class AppComponent {
  results_anim_state = 'middle';
  favorites_anim_state = 'middle';
  details_anim_state = 'middle';

  favres = 'res';


  latest_search_results = null;
  page = 'list';
  latest_requested_details_page_of = null;

  show_no_results_error = false;
  @ViewChild("resultsButton") results_button: ElementRef;

  search_error_occured = false;

  constructor(private details: DetailsService,
              private progress: ProgressService,
              private favbox: FavoritesBoxService) {}

  load_details_page(place) {
    this.page = "details";
    this.latest_requested_details_page_of = place;
    this.animateDetails();
  }

  favclicked() {

    this.favres='fav';
    if(this.page!=='list'){this.page='list';this.animateResults();}
  }

  resclicked() {

    this.favres='res';
    if(this.page!=='list'){this.page='list';this.animateResults();}

  }

  animateResults() {
    this.results_anim_state = 'left';
    setTimeout(()=>{this.results_anim_state = 'middle';});
  }

  animateFavorites() {
    this.favorites_anim_state = 'left';
    setTimeout(()=>{this.favorites_anim_state = 'middle';});
  }

  animateDetails() {
    this.details_anim_state = 'left';
    setTimeout(()=>{this.details_anim_state = 'middle';});
  }

  reset() {
    this.search_error_occured = false;
    this.latest_search_results = null;
    this.latest_requested_details_page_of = null;

    this.page = "list";
    this.show_no_results_error = false;

    this.details.place = {};
    this.details.details = {};
    this.results_button.nativeElement.click();
  }

  load_latest_search_results(results) {
    this.search_error_occured = false;
    if(!results) {
      this.show_no_results_error = true;
    } else {
      this.show_no_results_error = false;
    }
    this.page = 'list';
    this.latest_search_results = results;
    this.latest_requested_details_page_of = null;
    this.details.place = {};
    this.details.details = {};
    if(!this.results_button.nativeElement.classList.contains('active'))
      this.results_button.nativeElement.click();
  }

  search_error() {
    this.show_no_results_error = false;
    this.search_error_occured = true;
    this.page = 'list';
    this.results_button.nativeElement.click();
    this.details.place = {};
    this.details.details = {};
    this.latest_search_results = null;
  }
}
