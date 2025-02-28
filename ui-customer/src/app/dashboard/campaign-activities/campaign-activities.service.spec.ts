import { TestBed } from "@angular/core/testing";

import { CampaignActivitiesService } from "./campaign-activities.service";

describe("CampaignActivitiesService", () => {
    let service: CampaignActivitiesService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(CampaignActivitiesService);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });
});
