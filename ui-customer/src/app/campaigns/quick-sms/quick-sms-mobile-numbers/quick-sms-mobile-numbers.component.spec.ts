import { ComponentFixture, TestBed } from "@angular/core/testing";

import { QuickSmsMobileNumbersComponent } from "./quick-sms-mobile-numbers.component";

describe("QuickSmsMobileNumbersComponent", () => {
    let component: QuickSmsMobileNumbersComponent;
    let fixture: ComponentFixture<QuickSmsMobileNumbersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [QuickSmsMobileNumbersComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(QuickSmsMobileNumbersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
