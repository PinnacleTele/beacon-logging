import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignSenderIdComponent } from "./campaign-sender-id.component";

describe("CampaignSenderIdComponent", () => {
    let component: CampaignSenderIdComponent;
    let fixture: ComponentFixture<CampaignSenderIdComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignSenderIdComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignSenderIdComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
