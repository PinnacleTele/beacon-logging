import { ComponentFixture, TestBed } from "@angular/core/testing";

import { SingleFileUploadComponent } from "./single-file-upload.component";

describe("SingleFileUploadComponent", () => {
    let component: SingleFileUploadComponent;
    let fixture: ComponentFixture<SingleFileUploadComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [SingleFileUploadComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SingleFileUploadComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
