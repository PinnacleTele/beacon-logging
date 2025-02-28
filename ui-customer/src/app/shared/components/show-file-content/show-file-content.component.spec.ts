import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ShowFileContentComponent } from "./show-file-content.component";

describe("ShowFileContentComponent", () => {
    let component: ShowFileContentComponent;
    let fixture: ComponentFixture<ShowFileContentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ShowFileContentComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ShowFileContentComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
