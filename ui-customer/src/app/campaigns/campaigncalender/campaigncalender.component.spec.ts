import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaigncalenderComponent } from './campaigncalender.component';

describe('CampaigncalenderComponent', () => {
  let component: CampaigncalenderComponent;
  let fixture: ComponentFixture<CampaigncalenderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaigncalenderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaigncalenderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
