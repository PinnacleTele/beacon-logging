import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogDownloadComponent } from './log-download.component';

describe('LogDownloadComponent', () => {
  let component: LogDownloadComponent;
  let fixture: ComponentFixture<LogDownloadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LogDownloadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LogDownloadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
