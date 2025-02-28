import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupContactsAddComponent } from './group-contacts-add.component';

describe('GroupContactsAddComponent', () => {
  let component: GroupContactsAddComponent;
  let fixture: ComponentFixture<GroupContactsAddComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupContactsAddComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupContactsAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
