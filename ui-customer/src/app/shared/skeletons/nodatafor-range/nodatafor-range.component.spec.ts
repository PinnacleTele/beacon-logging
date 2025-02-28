import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NodataforRangeComponent } from './nodatafor-range.component';

describe('NodataforRangeComponent', () => {
  let component: NodataforRangeComponent;
  let fixture: ComponentFixture<NodataforRangeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NodataforRangeComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NodataforRangeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
