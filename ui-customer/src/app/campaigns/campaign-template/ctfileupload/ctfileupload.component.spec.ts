import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CtfileuploadComponent } from './ctfileupload.component';

describe('CtfileuploadComponent', () => {
  let component: CtfileuploadComponent;
  let fixture: ComponentFixture<CtfileuploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CtfileuploadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CtfileuploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
