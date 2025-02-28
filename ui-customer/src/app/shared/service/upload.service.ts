import { Injectable } from "@angular/core";
import {
    HttpClient,
    HttpParams,
    HttpRequest,
    HttpEvent,
    HttpHeaders
} from "@angular/common/http";
import { Observable, throwError } from "rxjs";
import { catchError, delay, map } from "rxjs/operators";
import { CONSTANTS_URL } from "../compaign.url";

@Injectable()
export class UploadService {
    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    SINGLE_FILE_UPLOAD_API_URL = CONSTANTS_URL.SINGLE_FILE_UPLOAD;

  //  file_upload_URL: string = this.BASE_URL + this.SINGLE_FILE_UPLOAD_API_URL;
    file_upload_URL: string = CONSTANTS_URL.FILE_UPLOAD_URL+CONSTANTS_URL.SINGLE_FILE_UPLOAD_URL;

    constructor(private http: HttpClient) {}

    // file from event.target.files[0]
     userParams = { frompage: "template", username: "testuser" }
    uploadFile(file2: File): Observable<HttpEvent<any>> {
        const formData = new FormData();

        formData.append("template","frompage" )
        formData.append("testuser","username" )
        formData.append("upload", file2);

        // userParams = { frompage: "campaign", username: "testuser" };
        const params = new HttpParams();
       
        const options = {
            headers: new HttpHeaders({
                Accept: "application/json",
                "Content-Type": "application/json"
            })
        };
     
        const req = new HttpRequest(
            "POST",
           this.file_upload_URL ,
            formData,
            options
        );
        
        return this.http.request(req);
    }
    
    uploadWithProgress(formData: FormData): Observable<any> {
        
        return this.http
            .post(this.file_upload_URL, formData, {
                observe: "events",
                reportProgress: true
            }).pipe(
                map((responseData : any) => {
                
                    return responseData as any;
                }),
                catchError((err) => {
                    
                    if (err.status == 0) {
                            
                        let setError = {
                            message : "HTTP Server Timed Out",
                            statusCode: "500",
                            error : " HTTP Server Timed Out"
                        }
                        this.handleError(setError);
                    }else {
                        this.handleError(err.error)
                    }
                    
                    return err.errors;
                    // return throwError(err);
                })
            );
            
    }

    private handleError(error: any) {
        
        return throwError(error);
    }
}
