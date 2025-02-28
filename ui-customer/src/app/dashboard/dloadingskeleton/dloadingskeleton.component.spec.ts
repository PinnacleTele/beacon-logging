import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DloadingskeletonComponent } from './dloadingskeleton.component';

describe('DloadingskeletonComponent', () => {
  let component: DloadingskeletonComponent;
  let fixture: ComponentFixture<DloadingskeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DloadingskeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DloadingskeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
