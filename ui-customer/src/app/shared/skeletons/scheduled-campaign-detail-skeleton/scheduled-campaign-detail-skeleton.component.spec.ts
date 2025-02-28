import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ScheduledCampaignDetailSkeletonComponent } from './scheduled-campaign-detail-skeleton.component';

describe('ScheduledCampaignDetailSkeletonComponent', () => {
  let component: ScheduledCampaignDetailSkeletonComponent;
  let fixture: ComponentFixture<ScheduledCampaignDetailSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ScheduledCampaignDetailSkeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ScheduledCampaignDetailSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
