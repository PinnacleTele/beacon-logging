import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignSendButtonComponent } from "./campaign-send-button.component";

describe("CampaignSendButtonComponent", () => {
    let component: CampaignSendButtonComponent;
    let fixture: ComponentFixture<CampaignSendButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignSendButtonComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignSendButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
