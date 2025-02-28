import { ComponentFixture, TestBed } from "@angular/core/testing";

import { QuickSmsComponent } from "./quick-sms.component";

describe("QuickSmsComponent", () => {
    let component: QuickSmsComponent;
    let fixture: ComponentFixture<QuickSmsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [QuickSmsComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(QuickSmsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
