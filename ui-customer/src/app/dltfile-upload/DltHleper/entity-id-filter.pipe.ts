import { Pipe, PipeTransform } from '@angular/core';
import { DltUploadService } from '../dltupload.service';

@Pipe({
  name: 'entityIdFilter'
})
export class EntityIdFilterPipe implements PipeTransform {
  constructor(private dltService: DltUploadService) { }
  transform(items: any[], searchelement: string) {
    if (!items) {
      return [];
    }
    if (!searchelement) {
      return items;
    }
    searchelement = searchelement.toLocaleLowerCase();
    const arr = items.filter((it) => {
      return it.dlt_entity_id.toLocaleLowerCase().includes(searchelement);

    });
    this.dltService.entityIdSearchData(arr);
    return arr;
    // console.log("filter");
  }

}
