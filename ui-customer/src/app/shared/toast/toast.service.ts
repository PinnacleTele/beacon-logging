import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import * as moment from "moment";
import { Toast } from "./toast";

@Injectable({
    providedIn: "root"
})
export class ToastService {
    private toastObs$ = new BehaviorSubject<Toast[]>([]);

    getToastObs(): Observable<Toast[]> {
        return this.toastObs$.asObservable();
    }

    addToast(toast: Toast) {
        

        toast.id = moment().valueOf();
        // gives old data + new data
        // this.toastObs$.next(this.toastObs$.getValue().concat([toast]));
        // gives only new data
        this.toastObs$.next([toast]);
    }

    clear() {
        this.toastObs$.next(undefined);
    }

}
