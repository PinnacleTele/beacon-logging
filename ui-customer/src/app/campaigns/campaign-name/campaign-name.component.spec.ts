import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignNameComponent } from "./campaign-name.component";

describe("CampaignNameComponent", () => {
    let component: CampaignNameComponent;
    let fixture: ComponentFixture<CampaignNameComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignNameComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignNameComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
