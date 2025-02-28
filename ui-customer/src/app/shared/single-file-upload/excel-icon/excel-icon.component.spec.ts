import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ExcelIconComponent } from './excel-icon.component';

describe('ExcelIconComponent', () => {
  let component: ExcelIconComponent;
  let fixture: ComponentFixture<ExcelIconComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ExcelIconComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ExcelIconComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
