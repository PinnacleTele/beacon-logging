import { ComponentFixture, TestBed } from "@angular/core/testing";

import { PreviewTemplateMessageComponent } from "./preview-template-message.component";

describe("PreviewTemplateMessageComponent", () => {
    let component: PreviewTemplateMessageComponent;
    let fixture: ComponentFixture<PreviewTemplateMessageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PreviewTemplateMessageComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PreviewTemplateMessageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
