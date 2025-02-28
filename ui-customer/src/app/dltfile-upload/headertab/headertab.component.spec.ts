import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HeadertabComponent } from './headertab.component';

describe('HeadertabComponent', () => {
  let component: HeadertabComponent;
  let fixture: ComponentFixture<HeadertabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ HeadertabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HeadertabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
