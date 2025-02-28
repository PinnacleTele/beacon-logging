import { TestBed } from "@angular/core/testing";

import { ScheduledCampaignsService } from "./scheduled-campaigns.service";

describe("ScheduledCampaignsService", () => {
    let service: ScheduledCampaignsService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(ScheduledCampaignsService);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });
});
