import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SchedulecalenderComponent } from './schedulecalender.component';

describe('SchedulecalenderComponent', () => {
  let component: SchedulecalenderComponent;
  let fixture: ComponentFixture<SchedulecalenderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SchedulecalenderComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SchedulecalenderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
