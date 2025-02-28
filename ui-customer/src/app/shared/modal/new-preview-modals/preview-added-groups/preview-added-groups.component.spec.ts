import { ComponentFixture, TestBed } from "@angular/core/testing";

import { PreviewAddedGroupsComponent } from "./preview-added-groups.component";

describe("PreviewAddedGroupsComponent", () => {
    let component: PreviewAddedGroupsComponent;
    let fixture: ComponentFixture<PreviewAddedGroupsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PreviewAddedGroupsComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PreviewAddedGroupsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
