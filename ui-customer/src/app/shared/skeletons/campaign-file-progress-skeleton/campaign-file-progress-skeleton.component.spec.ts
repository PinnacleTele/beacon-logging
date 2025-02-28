import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignFileProgressSkeletonComponent } from './campaign-file-progress-skeleton.component';

describe('CampaignFileProgressSkeletonComponent', () => {
  let component: CampaignFileProgressSkeletonComponent;
  let fixture: ComponentFixture<CampaignFileProgressSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignFileProgressSkeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignFileProgressSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
