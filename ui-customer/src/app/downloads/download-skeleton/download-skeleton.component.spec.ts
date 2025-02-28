import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DownloadSkeletonComponent } from './download-skeleton.component';

describe('DownloadSkeletonComponent', () => {
  let component: DownloadSkeletonComponent;
  let fixture: ComponentFixture<DownloadSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DownloadSkeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DownloadSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
