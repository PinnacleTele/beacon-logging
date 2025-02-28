import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UniquenamedropdownapierrorComponent } from './uniquenamedropdownapierror.component';

describe('UniquenamedropdownapierrorComponent', () => {
  let component: UniquenamedropdownapierrorComponent;
  let fixture: ComponentFixture<UniquenamedropdownapierrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ UniquenamedropdownapierrorComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UniquenamedropdownapierrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
