import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NodataerrorComponent } from './nodataerror.component';

describe('NodataerrorComponent', () => {
  let component: NodataerrorComponent;
  let fixture: ComponentFixture<NodataerrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NodataerrorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NodataerrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
