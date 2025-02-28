import { ComponentFixture, TestBed } from "@angular/core/testing";

import { SelectMobileColumnComponent } from "./select-mobile-column.component";

describe("SelectMobileColumnComponent", () => {
    let component: SelectMobileColumnComponent;
    let fixture: ComponentFixture<SelectMobileColumnComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SelectMobileColumnComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelectMobileColumnComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
