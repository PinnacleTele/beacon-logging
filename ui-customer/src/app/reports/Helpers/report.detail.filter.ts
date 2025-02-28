import { Pipe, PipeTransform } from "@angular/core";
import { reportDetailModel } from "../reports-detailed/report.detail.model";
import { ReportDetailService } from "./detail.service";
import { ReportService } from "./summary.service";
@Pipe({
    name: "ReportListFilter",
    pure: false
})
export class ReportListFilter implements PipeTransform {
    constructor(private RDservice:ReportService) {}

    public count: any;

    transform(items: reportDetailModel[], searchText: string): reportDetailModel[] {
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText.toLocaleLowerCase();
        const arr = items.filter((it) => {
            return (
                it.recv_time.toLocaleLowerCase().includes(searchText) ||
                it.del_dly_time.toLocaleLowerCase().includes(searchText) ||
                it.status.toLocaleLowerCase().includes(searchText) ||
                it.reason.toLocaleLowerCase().includes(searchText)  ||
                it.sub_msg.toLowerCase().includes(searchText) ||
                it.status.toLowerCase().includes(searchText) ||
                it.dest.toLowerCase().includes(searchText) ||
                it.sub_file_id.toLowerCase().includes(searchText) ||
                it.sub_cli_hdr.toLowerCase().includes(searchText) ||
                it.sub_carrier_sub_time.toLowerCase().includes(searchText) ||
                it.sms_rate.toLowerCase().includes(searchText) ||
                it.dlt_rate.toLowerCase().includes(searchText) 
            );
        });
        this.RDservice.searchData(arr);
        return arr;
    }
}
