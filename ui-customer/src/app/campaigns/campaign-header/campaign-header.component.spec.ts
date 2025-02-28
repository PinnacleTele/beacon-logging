import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CampaignHeaderComponent } from "./campaign-header.component";

describe("CampaignHeaderComponent", () => {
    let component: CampaignHeaderComponent;
    let fixture: ComponentFixture<CampaignHeaderComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [CampaignHeaderComponent]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CampaignHeaderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
