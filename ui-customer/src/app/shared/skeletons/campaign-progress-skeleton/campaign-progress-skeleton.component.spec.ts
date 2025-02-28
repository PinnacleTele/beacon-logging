import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignProgressSkeletonComponent } from './campaign-progress-skeleton.component';

describe('CampaignProgressSkeletonComponent', () => {
  let component: CampaignProgressSkeletonComponent;
  let fixture: ComponentFixture<CampaignProgressSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignProgressSkeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignProgressSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
