import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SelectedtrafficComponent } from './selectedtraffic.component';

describe('SelectedtrafficComponent', () => {
  let component: SelectedtrafficComponent;
  let fixture: ComponentFixture<SelectedtrafficComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SelectedtrafficComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SelectedtrafficComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
