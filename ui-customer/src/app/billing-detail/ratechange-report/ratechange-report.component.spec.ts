import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RatechangeReportComponent } from './ratechange-report.component';

describe('RatechangeReportComponent', () => {
  let component: RatechangeReportComponent;
  let fixture: ComponentFixture<RatechangeReportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RatechangeReportComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RatechangeReportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
