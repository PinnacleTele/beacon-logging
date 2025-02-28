// import { Pipe, PipeTransform } from "@angular/core";
// import { groupModel } from "../groupsMangement.group.model";

// import { SearchService } from "../groups.search.service";
// import { GroupModel } from "src/app/campaigns/model/campaign-group-model";

// @Pipe({
//     name: "groupFilter",
//     pure: false
// })
// export class GroupFilterPipe implements PipeTransform {
//     constructor(public searchvalue: SearchService) {}

//     public count: any;

//     transform(items: GroupModel[], searchText: string): GroupModel[] {
//         if (!items) {
//             return [];
//         }
//         if (!searchText) {
//             return items;
//         }
//         searchText = searchText.toLocaleLowerCase();
//         const arr = items.filter((it) => {
//             return (
//                 it.g_name.toLocaleLowerCase().includes(searchText) ||
//                 it.g_type.toLocaleLowerCase().includes(searchText) ||
//                 it.total
//                     .toString()
//                     .toLocaleLowerCase()
//                     .includes(searchText) ||
//                 it.g_visibility.toLocaleLowerCase().includes(searchText) ||
//                 it.created_ts.toLocaleLowerCase().includes(searchText)
//             );
//         });
//         this.count = arr.length;
//         console.log(arr,'in service');
        
//         this.searchvalue.searchData(arr)
//         this.searchvalue.searchcount(arr.length);
//         return arr;
//     }
// }


// import { Pipe, PipeTransform } from "@angular/core";
// import { GroupModel } from "src/app/campaigns/model/campaign-group-model";
// import { SearchService } from "../groups.search.service";
// @Pipe({
//     name: "Filter",
//     pure: false
// })
// export class FilterPipe implements PipeTransform {
//     constructor(public searchvalue: SearchService) {}

//     transform(items: GroupModel[], searchText: string): any {
//         if (!items) {
//             return [];
//         }
//         if (!searchText) {
//             return items;
//         }
//       const resultArray = [];
     
//         const arr = items.filter((it) => {
//             return (
//                 it.g_name.toLocaleLowerCase().includes(searchText) ||
//                 it.g_type.toLocaleLowerCase().includes(searchText) ||
//                 it.total
//                     .toString()
//                     .toLocaleLowerCase()
//                     .includes(searchText) ||
//                 it.g_visibility.toLocaleLowerCase().includes(searchText) ||
//                 it.created_ts.toLocaleLowerCase().includes(searchText)
//             );
//         });
//         this.searchvalue.searchData(arr)
//         this.searchvalue.searchcount(arr.length);
//       return arr;
//    }
//   }