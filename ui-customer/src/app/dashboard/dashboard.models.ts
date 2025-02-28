/* eslint-disable max-classes-per-file */
export class Summary {
    responsecode: number;

    responsestatus: string;

    data: {
        today: {
            count: string;
            total: string;
        };
        completed: {
            count: string;
            percantage: number;
        };
        running: {
            count: string;
            percantage: number;
        };
        failed: {
            count: string;
            percantage: number;
        };
    };
}

export class Campaign {
    campid: number;

    name: string;

    type: string;

    timestamp: string;

    total: string;

    message: string;

    status: string;
}

export class CampaignsList {
    responsecode: number;

    responsestatus: string;

    data: {
        total: string;
        completed: string;
        tabledata: Campaign[];
    };
}

export class Stats {
    accounts: string;

    groups: string;

    contacts: string;

    templates: string;
}

export class Statistics {
    responsecode: number;

    responsestatus: string;

    data: Stats;
}

export class Activity {
    pretext: string;

    posttext: string;

    campaign: {
        id: number;
        name: string;
        time: string;
    };
}

export class Activities {
    responsecode: number;

    responsestatus: string;

    data: Activity[];
}

export class ScheduledCampaign {
    campid: number;

    name: string;

    total: string;

    type: string;

    timeremaining?: string;

    message: string;

    date: string;

    time: string;
}

export class ScheduledCampaignsList {
    responsecode: number;

    responsestatus: string;

    data: {
        total: string;
        tabledata: ScheduledCampaign[];
    };
}
