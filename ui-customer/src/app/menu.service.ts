import { Injectable } from "@angular/core";

@Injectable({
    providedIn: "root"
})
export class MenuService {
    constructor() {}

    private data = {};

    public setStatus(option, value) {
        this.data[option] = value;
    }

    public getStatus() {
        return this.data;
    }
}
