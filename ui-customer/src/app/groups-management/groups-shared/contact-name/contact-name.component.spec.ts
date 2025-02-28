import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ContactNameComponent } from './contact-name.component';

describe('ContactNameComponent', () => {
  let component: ContactNameComponent;
  let fixture: ComponentFixture<ContactNameComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ContactNameComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ContactNameComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
