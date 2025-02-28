import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CScheduleListComponent } from './c-schedule-list.component';

describe('CScheduleListComponent', () => {
  let component: CScheduleListComponent;
  let fixture: ComponentFixture<CScheduleListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CScheduleListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CScheduleListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
