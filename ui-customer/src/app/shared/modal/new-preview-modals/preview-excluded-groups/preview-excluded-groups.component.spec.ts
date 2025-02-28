import { ComponentFixture, TestBed } from "@angular/core/testing";

import { PreviewExcludedGroupsComponent } from "./preview-excluded-groups.component";

describe("PreviewExcludedGroupsComponent", () => {
    let component: PreviewExcludedGroupsComponent;
    let fixture: ComponentFixture<PreviewExcludedGroupsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PreviewExcludedGroupsComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(PreviewExcludedGroupsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
