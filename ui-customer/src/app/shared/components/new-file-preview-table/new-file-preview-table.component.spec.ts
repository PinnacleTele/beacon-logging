import { ComponentFixture, TestBed } from "@angular/core/testing";

import { NewFilePreviewTableComponent } from "./new-file-preview-table.component";

describe("NewFilePreviewTableComponent", () => {
    let component: NewFilePreviewTableComponent;
    let fixture: ComponentFixture<NewFilePreviewTableComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [NewFilePreviewTableComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(NewFilePreviewTableComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
