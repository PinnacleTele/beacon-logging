import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CreateNewTemplateMessageComponent } from "./create-new-template-message.component";

describe("CreateNewTemplateMessageComponent", () => {
    let component: CreateNewTemplateMessageComponent;
    let fixture: ComponentFixture<CreateNewTemplateMessageComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CreateNewTemplateMessageComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateNewTemplateMessageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
