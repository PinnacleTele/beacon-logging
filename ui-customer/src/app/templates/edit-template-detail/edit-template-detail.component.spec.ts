import { ComponentFixture, TestBed } from "@angular/core/testing";

import { EditTemplateDetailComponent } from "./edit-template-detail.component";

describe("EditTemplateDetailComponent", () => {
    let component: EditTemplateDetailComponent;
    let fixture: ComponentFixture<EditTemplateDetailComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [EditTemplateDetailComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditTemplateDetailComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
