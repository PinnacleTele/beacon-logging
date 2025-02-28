import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignScheduledDetailComponent } from './campaign-scheduled-detail.component';

describe('CampaignScheduledDetailComponent', () => {
  let component: CampaignScheduledDetailComponent;
  let fixture: ComponentFixture<CampaignScheduledDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignScheduledDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignScheduledDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
