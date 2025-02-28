import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { BehaviorSubject, throwError } from "rxjs";
import { catchError, map } from "rxjs/operators";
import { CONSTANTS_URL } from "src/app/shared/compaign.url";
import { GroupModel } from "../model/campaign-group-model";

@Injectable()
export class GroupsCampaignService {
    constructor(private http: HttpClient) {}

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    GROUPS_URL="/cm/group/groupsForCampaign?g_type="

    GROUPS_END=this.BASE_URL+this.GROUPS_URL
    badError : any;
    

    //normalGroup (or) excludeGroup fetching API call

    findAllGroups(g_type:string){
      
      return  this.http.get<GroupModel[]>(this.GROUPS_END+g_type).pipe(
          map((responceData)=>{
            //  console.log(responceData);
            
             return responceData
              
          }),
          catchError((err) => {
            if (err.status == 0) {
                    
                let setError = {
                    message : "",
                    statusCode: "500",
                    error : "sharedGroups"
                }
                this.badError = setError;
            }else {

                this.badError = err.error;
            }
            
            
            return throwError(err);
        })
    );
}


    


    
}
