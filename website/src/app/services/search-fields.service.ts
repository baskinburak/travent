import { Injectable } from '@angular/core';

@Injectable()
export class SearchFieldsService {

  keyword: string = "";
  category: string = "default";
  distance: string = "";
  use_current_loc: boolean = true;
  location: string = "";

  reset() {
    this.keyword = "";
    this.category = "default";
    this.distance = "";
    this.use_current_loc = true;
    this.location = "";
  }

  constructor() { }

}
