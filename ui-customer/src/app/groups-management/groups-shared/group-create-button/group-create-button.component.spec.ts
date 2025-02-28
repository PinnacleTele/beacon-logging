import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupCreateButtonComponent } from './group-create-button.component';

describe('GroupCreateButtonComponent', () => {
  let component: GroupCreateButtonComponent;
  let fixture: ComponentFixture<GroupCreateButtonComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupCreateButtonComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(GroupCreateButtonComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
