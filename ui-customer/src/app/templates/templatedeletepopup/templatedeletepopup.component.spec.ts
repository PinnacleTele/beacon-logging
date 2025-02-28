import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TemplatedeletepopupComponent } from './templatedeletepopup.component';

describe('TemplatedeletepopupComponent', () => {
  let component: TemplatedeletepopupComponent;
  let fixture: ComponentFixture<TemplatedeletepopupComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ TemplatedeletepopupComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TemplatedeletepopupComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
