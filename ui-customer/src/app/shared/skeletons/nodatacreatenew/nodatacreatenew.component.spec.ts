import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NodatacreatenewComponent } from './nodatacreatenew.component';

describe('NodatacreatenewComponent', () => {
  let component: NodatacreatenewComponent;
  let fixture: ComponentFixture<NodatacreatenewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NodatacreatenewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NodatacreatenewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
