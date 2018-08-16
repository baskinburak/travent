import { Injectable, ElementRef } from '@angular/core';
import { ApiService } from './api.service';

@Injectable()
export class DetailsService {
  place = {};
  details = {};

  constructor(private api: ApiService) {
  }


}
