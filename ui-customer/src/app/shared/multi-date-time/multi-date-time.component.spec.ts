import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MultiDateTimeComponent } from './multi-date-time.component';

describe('MultiDateTimeComponent', () => {
  let component: MultiDateTimeComponent;
  let fixture: ComponentFixture<MultiDateTimeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MultiDateTimeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MultiDateTimeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
