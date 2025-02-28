import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignHomeComponent } from './campaign-home.component';

describe('CampaignHomeComponent', () => {
  let component: CampaignHomeComponent;
  let fixture: ComponentFixture<CampaignHomeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignHomeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignHomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
