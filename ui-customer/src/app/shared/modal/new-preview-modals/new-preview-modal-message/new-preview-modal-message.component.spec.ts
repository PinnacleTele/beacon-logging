import { ComponentFixture, TestBed } from "@angular/core/testing";

import { NewPreviewModalMessageComponent } from "./new-preview-modal-message.component";

describe("NewPreviewModalMessageComponent", () => {
    let component: NewPreviewModalMessageComponent;
    let fixture: ComponentFixture<NewPreviewModalMessageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [NewPreviewModalMessageComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(NewPreviewModalMessageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
