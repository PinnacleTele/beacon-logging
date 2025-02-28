import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { TemplateModel } from "./model/templatemodal";
// import { TemplateList } from "./Helpers/data";
@Injectable({
    providedIn: "root"
})
export class SearchService {
    searchArrayCount = 0;
 
    ArrayCount = 0;

    totalRecords: any = [];
 
    dataRecords: any = [];
 
    templatesDetail: any = TemplateModel;
 
    // TemplateList :TemplateModel[] = TemplateList;
    viewTemplatesId: any;
 
    userDataSource: BehaviorSubject<Array<any>> = new BehaviorSubject([]);
 
    userData = this.userDataSource.asObservable();
 
    constructor() {}
 
    public searchcount(value: any): Observable<any> {
        if (value === 0) {
            this.searchArrayCount = 0;
        }
        return (this.searchArrayCount = value);
    }
 
    public searchData(value: any): Observable<any> {
        
        this.userDataSource.next(value);
        this.ArrayCount = value.length;
        return (this.totalRecords = value);
    }

    get Count(){
        return  this.ArrayCount;
    }
 
    // getTemplateByGivenId(id : any) {
    //     this.templatesDetail = this.TemplateList.find(cus => cus.id === id);
    //     return new Promise((resolve, reject) => {
    //       setTimeout(() => {
    //         resolve(this.templatesDetail);
    //       }, 1000);
    //     });
    //   }
 
    // getAllTemplates() {
    //   return new Promise((resolve, reject) => {
    //   //   setTimeout(() => {
    //       const data = { res: this.TemplateList };
    //       resolve(data);
    //   //   }, 1000);
    //   });
    // }
 
    setViewTemplatesId(id: any) {
        this.viewTemplatesId = id;
    }
 
    getViewTemplatesId() {
        return this.viewTemplatesId;
    }
}