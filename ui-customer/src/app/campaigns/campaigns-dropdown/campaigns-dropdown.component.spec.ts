import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampaignsDropdownComponent } from './campaigns-dropdown.component';

describe('CampaignsDropdownComponent', () => {
  let component: CampaignsDropdownComponent;
  let fixture: ComponentFixture<CampaignsDropdownComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CampaignsDropdownComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CampaignsDropdownComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
