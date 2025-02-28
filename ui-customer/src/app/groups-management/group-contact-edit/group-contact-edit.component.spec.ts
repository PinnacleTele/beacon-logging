import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupContactEditComponent } from './group-contact-edit.component';

describe('GroupContactEditComponent', () => {
  let component: GroupContactEditComponent;
  let fixture: ComponentFixture<GroupContactEditComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupContactEditComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupContactEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
