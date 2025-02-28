import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DltfileUploadComponent } from './dltfile-upload.component';

describe('DltfileUploadComponent', () => {
  let component: DltfileUploadComponent;
  let fixture: ComponentFixture<DltfileUploadComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DltfileUploadComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DltfileUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
