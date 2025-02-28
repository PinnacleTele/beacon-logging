
import { Pipe, PipeTransform } from "@angular/core";
import { requestModel } from '../log-download/reqest.model';
import { DownloadsService } from './downloads-service.service';
@Pipe({
    name: "downloadFilter",
    pure: false
})
export class DownloadFilterPipe implements PipeTransform {
    constructor( private downloadsservice:DownloadsService) {}

    public count: any;

    transform(items:  requestModel[], searchText: string): requestModel[]{
        if (!items) {
            return [];
        }
        if (!searchText) {
            return items;
        }
        searchText = searchText?.toLocaleLowerCase();
        const arr = items.filter((it) => {
          it.total = it.total + ""
            return (
                it.from?.toLocaleLowerCase().includes(searchText) ||
                it.to?.toLocaleLowerCase().includes(searchText) ||
                it.status?.toLocaleLowerCase().includes(searchText) ||
                it.total?.toLocaleLowerCase().includes(searchText)  ||
                it.created_ts?.toLowerCase().includes(searchText)   
               
            );
        });
        
        this.downloadsservice.searchData(arr);
        return arr;
    }
}