import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaigncardDetailComponent } from './campaigncard-detail.component';

describe('CampaigncardDetailComponent', () => {
  let component: CampaigncardDetailComponent;
  let fixture: ComponentFixture<CampaigncardDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaigncardDetailComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaigncardDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
