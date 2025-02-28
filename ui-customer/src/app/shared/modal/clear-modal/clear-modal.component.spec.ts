import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ClearModalComponent } from "./clear-modal.component";

describe("ClearModalComponent", () => {
    let component: ClearModalComponent;
    let fixture: ComponentFixture<ClearModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ClearModalComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ClearModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
