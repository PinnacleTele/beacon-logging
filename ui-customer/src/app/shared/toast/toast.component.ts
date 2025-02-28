import { Component, OnDestroy, OnInit } from "@angular/core";
import { Subscription } from "rxjs";
import * as moment from "moment";
import { Toast } from "./toast";
import { ToastService } from "./toast.service";
import { EnterExitRight, Container1 } from "../animation";
import { CONSTANTS } from "../campaigns.constants";
import { Router } from "@angular/router";
@Component({
    selector: "app-toast",
    templateUrl: "./toast.component.html",
    styleUrls: ["./toast.component.css"],
    animations: [Container1, EnterExitRight]
})
export class ToastComponent implements OnInit, OnDestroy {
    private toastsMap: Map<number, Toast> = new Map<number, Toast>();

    private toastTimeout: Map<number, string> = new Map<number, string>();

    private toastTimer: Map<number, any> = new Map<number, any>();

    constructor(private toastService: ToastService, private router:Router) {}

    subscription$: Subscription;

    toastData: Toast[] = [];

    toastTimeToLiveInSeconds = 5;

    timeStampFormat = CONSTANTS.TIMESTAMP_FORMAT;

    ngOnInit(): void {
        // this.notificationsCleaning();
        this.subscription$ = this.toastService
            .getToastObs()
            // eslint-disable-next-line no-return-assign
            .subscribe((toasts: Toast[]) => {
                if (toasts && toasts.length > 0) {
                    toasts.forEach((toast) => {
                        this.toastsMap.set(toast.id, toast);
                        if (toast.autoClose) {
                            this.toastTimeout.set(
                                toast.id,
                                moment()
                                    .add(
                                        this.toastTimeToLiveInSeconds,
                                        "seconds"
                                    )
                                    .format(this.timeStampFormat)
                            );
                            this.setTimeout(toast.id);
                        }
                    });
                    this.resetToastsList();
                }
            });
    }

    // attaches timer for toast with autoclose true
    setTimeout(toastId: number) {
        const timer = setTimeout(() => {
            this.autoCloseNotifications();
        }, this.toastTimeToLiveInSeconds * 1000);
        this.toastTimer.set(toastId, timer);
    }

    resetToastsList() {
        this.toastData = [];
        this.toastsMap.forEach((toast) => {
            this.toastData.push(toast);
        });
        if (this.toastData.length > 0) {
            this.toastData.sort((a: Toast, b: Toast) => b.id - a.id);
        }
    }

    trackById(index: number, toast: Toast): number {
        return toast.id;
    }

    autoCloseNotifications() {
        // console.log(" clearNotification called....");
        const deletedToasts: number[] = [];
        this.toastTimeout.forEach((value, key) => {
            if (moment().isSameOrAfter(moment(value, this.timeStampFormat))) {
                this.toastsMap.delete(key);
                deletedToasts.push(key);
            }
        });
        if (deletedToasts.length > 0) {
            deletedToasts.forEach((toastId) => {
                this.toastTimeout.delete(toastId);
            });
            this.resetToastsList();
        }
    }

    closeNotification(toast: Toast) {
        clearTimeout(this.toastTimer.get(toast.id));
        this.toastTimer.delete(toast.id);
        this.toastsMap.delete(toast.id);
        this.toastTimeout.delete(toast.id);
        this.resetToastsList();
    }

    toDownload(event:any){
        this.closeNotification(event);
        this.router.navigate(['/downloads/log']);
    }

    ngOnDestroy() {
        this.toastService.clear();
        this.subscription$.unsubscribe();
    }
}
