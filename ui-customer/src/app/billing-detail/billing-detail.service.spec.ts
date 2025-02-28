import { TestBed } from '@angular/core/testing';

import { BillingDetailService } from './billing-detail.service';

describe('BillingDetailService', () => {
  let service: BillingDetailService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(BillingDetailService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
