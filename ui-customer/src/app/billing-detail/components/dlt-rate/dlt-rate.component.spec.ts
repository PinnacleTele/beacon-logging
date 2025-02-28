import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DltRateComponent } from './dlt-rate.component';

describe('DltRateComponent', () => {
  let component: DltRateComponent;
  let fixture: ComponentFixture<DltRateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DltRateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DltRateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
