import { ComponentFixture, TestBed } from "@angular/core/testing";

import { SelectDltComponent } from "./select-dlt.component";

describe("SelectDltComponent", () => {
    let component: SelectDltComponent;
    let fixture: ComponentFixture<SelectDltComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SelectDltComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SelectDltComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
