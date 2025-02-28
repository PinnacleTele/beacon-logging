import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignSaveButtonComponent } from "./campaign-save-button.component";

describe("CampaignSaveButtonComponent", () => {
    let component: CampaignSaveButtonComponent;
    let fixture: ComponentFixture<CampaignSaveButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignSaveButtonComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignSaveButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
