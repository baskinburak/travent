import { Injectable } from '@angular/core';

@Injectable()
export class FavoritesBoxService {

  favorites = [];

  constructor() {
    this.load();
  }

  save() {
    window.localStorage.setItem("hw8favbox", JSON.stringify(this.favorites));
  }

  load() {
    let favsstr = window.localStorage.getItem("hw8favbox");
    if(favsstr)
      this.favorites = JSON.parse(favsstr);
  }

  put(place) {
    this.favorites.push(place);
    this.save();
  }

  get(placeid) {
    for(let i=0; i<this.favorites.length; i++) {
      let place = this.favorites[i];
      if(place.hasOwnProperty("id") && place.id == placeid) {
        return place;
      }
    }
    return null;
  }

  remove(placeid) {
    for(let i=0; i<this.favorites.length; i++) {
      let place = this.favorites[i];
      if(place.hasOwnProperty("id") && place.id == placeid) {
        this.favorites.splice(i, 1);
      }
    }
    this.save();
  }

  iterator() {
    let idx = 0;
    return function() {
      let x = idx;
      if(this.favorites.length > x) {
        idx++;
        return this.favorites[x];
      } else {
        return null;
      }
    }.bind(this);
  }

  exists(placeid) {
    for(let i=0; i<this.favorites.length; i++) {
      let place = this.favorites[i];
      if(place.hasOwnProperty("id") && place.id == placeid) {
        return true;
      }
    }
    return false;
  }

  toggle(place) {
    if(!place.hasOwnProperty("name"))
      return;
    if(this.exists(place.id)) {
      this.remove(place.id);
    } else {
      this.put(place);
    }
  }

}
