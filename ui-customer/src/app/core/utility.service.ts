import { Injectable } from "@angular/core";
import * as moment from "moment";
import { BehaviorSubject } from "rxjs";
import { CONSTANTS } from "../shared/campaigns.constants";
@Injectable({
    providedIn: "root"
})
export class UtilityService {
    // NOTE: Formats string/number to higher mb/gb etc
   
fileUploadProgress=new BehaviorSubject(false);

    formatBytes(bytes: any, decimals = 2): string {
        if (bytes === 0) return "0 Bytes";

        const k = 1024;
        const dm = decimals < 0 ? 0 : decimals;
        const sizes = ["Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"];

        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return `${parseFloat((bytes / Math.pow(k, i)).toFixed(dm))} ${
            sizes[i]
        }`;
    }

    convertTimeZone(
        inputDate: string,
        inputDateFormat: string,
        targetOffSet: string,
        targetDateFormat: string = CONSTANTS.DATE_AND_TIME_FORMAT
    ): string {
        const fromOffSetInMinutes = moment(inputDate, inputDateFormat)
            .parseZone()
            .utcOffset();
        const toOffSetInMinutes = moment().utcOffset(targetOffSet).utcOffset();
        const inputMomentObject = moment(inputDate, inputDateFormat).utcOffset(
            fromOffSetInMinutes
        );
        const targetMomentObject = inputMomentObject
            .clone()
            .utcOffset(toOffSetInMinutes);
         //  console.log(targetMomentObject.format(targetDateFormat))
        return targetMomentObject.format(targetDateFormat);
    }


}
