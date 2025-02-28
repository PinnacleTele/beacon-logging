import { ComponentFixture, TestBed } from "@angular/core/testing";

import { PreviewModalDateTimeComponent } from "./preview-modal-date-time.component";

describe("PreviewModalDateTimeComponent", () => {
    let component: PreviewModalDateTimeComponent;
    let fixture: ComponentFixture<PreviewModalDateTimeComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PreviewModalDateTimeComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PreviewModalDateTimeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
