import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReportCalenderComponent } from './report-calender.component';

describe('ReportCalenderComponent', () => {
  let component: ReportCalenderComponent;
  let fixture: ComponentFixture<ReportCalenderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ReportCalenderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ReportCalenderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
