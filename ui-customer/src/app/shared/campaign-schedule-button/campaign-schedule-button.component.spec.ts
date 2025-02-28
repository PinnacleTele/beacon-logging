import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignScheduleButtonComponent } from "./campaign-schedule-button.component";

describe("CampaignScheduleButtonComponent", () => {
    let component: CampaignScheduleButtonComponent;
    let fixture: ComponentFixture<CampaignScheduleButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignScheduleButtonComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignScheduleButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
