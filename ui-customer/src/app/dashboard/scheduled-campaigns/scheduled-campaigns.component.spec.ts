import { ComponentFixture, TestBed } from "@angular/core/testing";

import { ScheduledCampaignsComponent } from "./scheduled-campaigns.component";

describe("ScheduledCampaignsComponent", () => {
    let component: ScheduledCampaignsComponent;
    let fixture: ComponentFixture<ScheduledCampaignsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ScheduledCampaignsComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ScheduledCampaignsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
