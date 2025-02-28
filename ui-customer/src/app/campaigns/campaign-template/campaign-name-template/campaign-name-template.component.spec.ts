import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignNameTemplateComponent } from './campaign-name-template.component';

describe('CampaignNameTemplateComponent', () => {
  let component: CampaignNameTemplateComponent;
  let fixture: ComponentFixture<CampaignNameTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignNameTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignNameTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
