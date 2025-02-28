import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DltentityidComponent } from './dltentityid.component';

describe('DltentityidComponent', () => {
  let component: DltentityidComponent;
  let fixture: ComponentFixture<DltentityidComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DltentityidComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DltentityidComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
