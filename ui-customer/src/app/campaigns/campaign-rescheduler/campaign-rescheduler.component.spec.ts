import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignReschedulerComponent } from './campaign-rescheduler.component';

describe('CampaignReschedulerComponent', () => {
  let component: CampaignReschedulerComponent;
  let fixture: ComponentFixture<CampaignReschedulerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignReschedulerComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignReschedulerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
