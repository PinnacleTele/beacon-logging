import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ExcludeGroupComponent } from "./exclude-group.component";

describe("ExcludeGroupComponent", () => {
    let component: ExcludeGroupComponent;
    let fixture: ComponentFixture<ExcludeGroupComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ExcludeGroupComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ExcludeGroupComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
