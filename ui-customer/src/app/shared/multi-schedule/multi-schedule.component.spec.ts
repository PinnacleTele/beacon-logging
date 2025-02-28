import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiScheduleComponent } from './multi-schedule.component';

describe('MultiScheduleComponent', () => {
  let component: MultiScheduleComponent;
  let fixture: ComponentFixture<MultiScheduleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultiScheduleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiScheduleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
