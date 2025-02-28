import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DropdownfieldapierrorComponent } from './dropdownfieldapierror.component';

describe('DropdownfieldapierrorComponent', () => {
  let component: DropdownfieldapierrorComponent;
  let fixture: ComponentFixture<DropdownfieldapierrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DropdownfieldapierrorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DropdownfieldapierrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
