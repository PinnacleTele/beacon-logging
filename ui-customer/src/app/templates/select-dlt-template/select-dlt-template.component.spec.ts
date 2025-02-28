import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectDltTemplateComponent } from './select-dlt-template.component';

describe('SelectDltTemplateComponent', () => {
  let component: SelectDltTemplateComponent;
  let fixture: ComponentFixture<SelectDltTemplateComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectDltTemplateComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectDltTemplateComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
