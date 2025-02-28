import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RcreportCalendarComponent } from './rcreport-calendar.component';

describe('RcreportCalendarComponent', () => {
  let component: RcreportCalendarComponent;
  let fixture: ComponentFixture<RcreportCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RcreportCalendarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RcreportCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
