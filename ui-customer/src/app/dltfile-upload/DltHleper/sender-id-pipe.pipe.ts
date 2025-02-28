import { Pipe, PipeTransform } from '@angular/core';
import { DltUploadService } from '../dltupload.service';

@Pipe({
  name: 'senderIdPipe'
})
export class SenderIdPipePipe implements PipeTransform {
  constructor(private dltService: DltUploadService) {

  }
  transform(items: any[], searchelement: string) {
    if (!items) {
      return [];
    }
    if (!searchelement) {
      return items;
    }
    searchelement = searchelement.toLocaleLowerCase();
    const arr = items.filter((it) => {
      return it.header.toLocaleLowerCase().includes(searchelement);

    });
    this.dltService.senderIdSearchData(arr);
    return arr;
    // console.log("filter");
  }

}
