import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoadingskeletonComponent } from './loadingskeleton.component';

describe('LoadingskeletonComponent', () => {
  let component: LoadingskeletonComponent;
  let fixture: ComponentFixture<LoadingskeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoadingskeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoadingskeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
