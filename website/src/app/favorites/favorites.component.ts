import { Component, OnInit, Output, EventEmitter } from '@angular/core';
import { FavoritesBoxService } from '../services/favorites-box.service';
import { DetailsService } from '../services/details.service';

@Component({
  selector: 'app-favorites',
  templateUrl: './favorites.component.html',
  styleUrls: ['./favorites.component.css']
})
export class FavoritesComponent implements OnInit {

  page = 0;

  @Output("details-clicked") details_clicked = new EventEmitter();

  constructor(private favbox: FavoritesBoxService,
              private details: DetailsService) { }

  ngOnInit() {
  }

  load_details(place) {
    this.details_clicked.emit(place);
  }

  nextpage() {
    this.page++;
  }

  prevpage() {
    this.page--;
  }

  page_drop() {
    if(this.page > 0 && (this.page)*20 === this.favbox.favorites.length) {
      this.page--;
    }
  }
  ngDoCheck() {
    while(this.page > 0 && (this.page)*20 > this.favbox.favorites.length) {
      this.page--;
    }
  }

}
