import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { AppComponent } from './app.component';
import { SearchComponent } from './search/search.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { CurrentLocationService } from './services/current-location.service';
import { ApiService } from './services/api.service';
import { DetailsService } from './services/details.service';
import { FavoritesBoxService } from './services/favorites-box.service';
import { ResultsComponent } from './results/results.component';
import { FavoritesComponent } from './favorites/favorites.component';
import { DetailsComponent } from './details/details.component';
import { SearchFieldsService } from './services/search-fields.service';
import { ProgressService } from './services/progress.service';


@NgModule({
  declarations: [
    AppComponent,
    SearchComponent,
    ResultsComponent,
    FavoritesComponent,
    DetailsComponent

  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    HttpClientModule,
    NgbModule.forRoot(),
    BrowserAnimationsModule


  ],
  providers: [CurrentLocationService, ApiService, FavoritesBoxService, DetailsService, SearchFieldsService, ProgressService],
  bootstrap: [AppComponent]
})
export class AppModule { }
