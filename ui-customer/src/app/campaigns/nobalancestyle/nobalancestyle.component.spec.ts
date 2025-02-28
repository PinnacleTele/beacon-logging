import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NobalancestyleComponent } from './nobalancestyle.component';

describe('NobalancestyleComponent', () => {
  let component: NobalancestyleComponent;
  let fixture: ComponentFixture<NobalancestyleComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NobalancestyleComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(NobalancestyleComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
