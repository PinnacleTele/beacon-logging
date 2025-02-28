import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CommondeleteerrorpopupComponent } from './commondeleteerrorpopup.component';

describe('CommondeleteerrorpopupComponent', () => {
  let component: CommondeleteerrorpopupComponent;
  let fixture: ComponentFixture<CommondeleteerrorpopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ CommondeleteerrorpopupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CommondeleteerrorpopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
