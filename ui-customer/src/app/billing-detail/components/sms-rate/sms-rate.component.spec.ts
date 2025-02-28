import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SMSRateComponent } from './sms-rate.component';

describe('SMSRateComponent', () => {
  let component: SMSRateComponent;
  let fixture: ComponentFixture<SMSRateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SMSRateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SMSRateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
