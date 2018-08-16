import { TestBed, inject } from '@angular/core/testing';

import { SearchFieldsService } from './search-fields.service';

describe('SearchFieldsService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [SearchFieldsService]
    });
  });

  it('should be created', inject([SearchFieldsService], (service: SearchFieldsService) => {
    expect(service).toBeTruthy();
  }));
});
