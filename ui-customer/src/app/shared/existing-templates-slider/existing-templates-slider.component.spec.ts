import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExistingTemplatesSliderComponent } from './existing-templates-slider.component';

describe('ExistingTemplatesSliderComponent', () => {
  let component: ExistingTemplatesSliderComponent;
  let fixture: ComponentFixture<ExistingTemplatesSliderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExistingTemplatesSliderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExistingTemplatesSliderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
