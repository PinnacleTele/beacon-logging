import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditSkeletonComponent } from './edit-skeleton.component';

describe('EditSkeletonComponent', () => {
  let component: EditSkeletonComponent;
  let fixture: ComponentFixture<EditSkeletonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ EditSkeletonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditSkeletonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
