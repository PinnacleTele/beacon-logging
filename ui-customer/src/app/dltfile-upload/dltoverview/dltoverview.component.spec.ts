import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DltoverviewComponent } from './dltoverview.component';

describe('DltoverviewComponent', () => {
  let component: DltoverviewComponent;
  let fixture: ComponentFixture<DltoverviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DltoverviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DltoverviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
