import { ComponentFixture, TestBed } from "@angular/core/testing";

import { FileBasedOnComponent } from "./file-based-on.component";

describe("FileBasedOnComponent", () => {
    let component: FileBasedOnComponent;
    let fixture: ComponentFixture<FileBasedOnComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [FileBasedOnComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(FileBasedOnComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
