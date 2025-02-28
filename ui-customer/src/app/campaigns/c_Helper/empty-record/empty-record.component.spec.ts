import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EmptyRecordComponent } from './empty-record.component';

describe('EmptyRecordComponent', () => {
  let component: EmptyRecordComponent;
  let fixture: ComponentFixture<EmptyRecordComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EmptyRecordComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EmptyRecordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
