import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaigncardComponent } from './campaigncard.component';

describe('CampaigncardComponent', () => {
  let component: CampaigncardComponent;
  let fixture: ComponentFixture<CampaigncardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaigncardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaigncardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
