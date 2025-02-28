import { ComponentFixture, TestBed } from "@angular/core/testing";

import { NewPreviewModalsComponent } from "./new-preview-modals.component";

describe("NewPreviewModalsComponent", () => {
    let component: NewPreviewModalsComponent;
    let fixture: ComponentFixture<NewPreviewModalsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [NewPreviewModalsComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(NewPreviewModalsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
