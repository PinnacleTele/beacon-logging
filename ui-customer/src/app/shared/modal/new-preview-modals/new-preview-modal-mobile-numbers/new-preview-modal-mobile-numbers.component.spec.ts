import { ComponentFixture, TestBed } from "@angular/core/testing";

import { NewPreviewModalMobileNumbersComponent } from "./new-preview-modal-mobile-numbers.component";

describe("NewPreviewModalMobileNumbersComponent", () => {
    let component: NewPreviewModalMobileNumbersComponent;
    let fixture: ComponentFixture<NewPreviewModalMobileNumbersComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [NewPreviewModalMobileNumbersComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(
            NewPreviewModalMobileNumbersComponent
        );
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
