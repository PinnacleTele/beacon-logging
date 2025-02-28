import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TemplateCreateButtonComponent } from "./template-create-button.component";

describe("TemplateCreateButtonComponent", () => {
    let component: TemplateCreateButtonComponent;
    let fixture: ComponentFixture<TemplateCreateButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [TemplateCreateButtonComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(TemplateCreateButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
