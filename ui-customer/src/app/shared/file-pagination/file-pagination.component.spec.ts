import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FilePaginationComponent } from './file-pagination.component';

describe('FilePaginationComponent', () => {
  let component: FilePaginationComponent;
  let fixture: ComponentFixture<FilePaginationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FilePaginationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FilePaginationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
