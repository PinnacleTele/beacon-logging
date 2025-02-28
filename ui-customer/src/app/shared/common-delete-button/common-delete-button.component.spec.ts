import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommonDeleteButtonComponent } from './common-delete-button.component';

describe('CommonDeleteButtonComponent', () => {
  let component: CommonDeleteButtonComponent;
  let fixture: ComponentFixture<CommonDeleteButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CommonDeleteButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CommonDeleteButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
