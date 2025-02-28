import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloaderrorpopupComponent } from './downloaderrorpopup.component';

describe('DownloaderrorpopupComponent', () => {
  let component: DownloaderrorpopupComponent;
  let fixture: ComponentFixture<DownloaderrorpopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DownloaderrorpopupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloaderrorpopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
