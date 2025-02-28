import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignCancelButtonComponent } from "./campaign-cancel-button.component";

describe("CampaignCancelComponent", () => {
    let component: CampaignCancelButtonComponent;
    let fixture: ComponentFixture<CampaignCancelButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignCancelButtonComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignCancelButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
