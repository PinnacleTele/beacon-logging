export class MobileCountStatistics {
    constructor(
        public mobileNumbers: string[],
        public totalCount: number,
        public validCount: number,
        public uniqueCount: number,
        public invalidCount: number,
        public uniqueNumbers: string[],
        public invalidNumbers?: string[]
    ) {}
}
