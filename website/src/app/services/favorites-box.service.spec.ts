import { TestBed, inject } from '@angular/core/testing';

import { FavoritesBoxService } from './favorites-box.service';

describe('FavoritesBoxService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FavoritesBoxService]
    });
  });

  it('should be created', inject([FavoritesBoxService], (service: FavoritesBoxService) => {
    expect(service).toBeTruthy();
  }));
});
