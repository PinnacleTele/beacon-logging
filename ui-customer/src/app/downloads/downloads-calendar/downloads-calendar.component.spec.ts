import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadsCalendarComponent } from './downloads-calendar.component';

describe('DownloadsCalendarComponent', () => {
  let component: DownloadsCalendarComponent;
  let fixture: ComponentFixture<DownloadsCalendarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DownloadsCalendarComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadsCalendarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
