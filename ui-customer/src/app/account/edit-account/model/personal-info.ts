export class PersonalInfo {
    constructor(
        public firstName: string = "default",
        public lastName: string,
        public company: string,
        public currency: string,
        public address?: string
    ) {}
}