import { ComponentFixture, TestBed } from "@angular/core/testing";

import { DateComponent } from "./date-picker.component";

describe("DatePickerComponent", () => {
    let component: DateComponent;
    let fixture: ComponentFixture<DateComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DateComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(DateComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
