import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyAcctQcsettingsComponent } from './my-acct-qcsettings.component';

describe('MyAcctQcsettingsComponent', () => {
  let component: MyAcctQcsettingsComponent;
  let fixture: ComponentFixture<MyAcctQcsettingsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MyAcctQcsettingsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyAcctQcsettingsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
