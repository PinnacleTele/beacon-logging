import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable } from "rxjs";
import { GroupModel } from "./groupsMangement.group.model";
@Injectable({
    providedIn: "root"
})
export class SearchService {
    searchArrayCount = 0;

    ArrayCount = 0;


    totalRecords: any = [];

    dataRecords: any = [];

  
    templatesDetail: any = GroupModel;

    // TemplateList :TemplateModel[] = TemplateList;
    viewTemplatesId: any;  
    userDataSource: BehaviorSubject<Array<any>> = new BehaviorSubject([]);
 
    userData = this.userDataSource.asObservable();

    public searchcount(value: any): Observable<any> {
        if (value === 0) {
            this.searchArrayCount = 0;
        }
        return this, (this.searchArrayCount = value)
    }

    public searchData(value: any): Observable<any> {
        this.userDataSource.next(value);
        this.ArrayCount = value.length;
        return (this.totalRecords = value);
    }

    // public searchedData(value: any): Observable<any> {
    //     this.userDataSource.next(value);
    //     return (this.totalRecords = value);
    // }
    setViewTemplatesId(id: any) {
        this.viewTemplatesId = id;
    }

    getViewTemplatesId() {
        return this.viewTemplatesId;
    }
}
