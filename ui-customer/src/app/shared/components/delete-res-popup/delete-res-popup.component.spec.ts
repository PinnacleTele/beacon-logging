import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DeleteResPopupComponent } from './delete-res-popup.component';

describe('DeleteResPopupComponent', () => {
  let component: DeleteResPopupComponent;
  let fixture: ComponentFixture<DeleteResPopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DeleteResPopupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteResPopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
