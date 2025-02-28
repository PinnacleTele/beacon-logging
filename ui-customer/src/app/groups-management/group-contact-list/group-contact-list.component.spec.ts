import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupContactListComponent } from './group-contact-list.component';

describe('GroupContactListComponent', () => {
  let component: GroupContactListComponent;
  let fixture: ComponentFixture<GroupContactListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupContactListComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupContactListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
