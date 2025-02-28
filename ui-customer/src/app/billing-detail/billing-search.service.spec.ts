import { TestBed } from '@angular/core/testing';

import { BillingSearchService } from './billing-search.service';

describe('BillingSearchService', () => {
  let service: BillingSearchService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BillingSearchService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
