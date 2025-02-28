import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NorecordsDaterangeComponent } from './norecords-daterange.component';

describe('NorecordsDaterangeComponent', () => {
  let component: NorecordsDaterangeComponent;
  let fixture: ComponentFixture<NorecordsDaterangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NorecordsDaterangeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NorecordsDaterangeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
