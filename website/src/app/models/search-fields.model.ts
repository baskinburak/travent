export class SearchFields {
  keyword: string = "";
  category: string = "default";
  distance: number = 10;
  use_current_loc: boolean = true;
  location: string = "";

  reset() {
    this.keyword = "";
    this.category = "default";
    this.distance = 10;
    this.use_current_loc = true;
    this.location = "";
  }
}
