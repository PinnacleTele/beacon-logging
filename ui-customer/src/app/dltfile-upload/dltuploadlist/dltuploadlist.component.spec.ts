import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DltuploadlistComponent } from './dltuploadlist.component';

describe('DltuploadlistComponent', () => {
  let component: DltuploadlistComponent;
  let fixture: ComponentFixture<DltuploadlistComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DltuploadlistComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DltuploadlistComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
