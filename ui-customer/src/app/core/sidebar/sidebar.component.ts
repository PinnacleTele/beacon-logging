import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { Container, EnterExitLeft } from "../../shared/animation";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { CommonService } from "src/app/shared/commonservice";
import { HttpClient } from "@angular/common/http";
import { DomSanitizer } from "@angular/platform-browser";
import { Observable } from "rxjs";

@Component({
    selector: "app-sidebar",
    templateUrl: "./sidebar.component.html",
    styleUrls: ["./sidebar.component.css"],
    animations: [Container, EnterExitLeft]
})
export class SidebarComponent implements OnInit {
    _showOverlay = false;

    @Output() hideOverlayEvent: EventEmitter<boolean> = new EventEmitter();

    public LOGO: any;
    state = "normal";

    clusterType :any;

    @Input() activeLink = "";

    user: any;

    billingpath = '';

    activeLinkStyle =
        "bg-gray-900 text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

    inActiveLinkStyle =
        "group text-gray-400 hover:bg-gray-900 hover:text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

    activeLinkStyleForSmallResoltions =
        "bg-gray-900 text-white py-2 px-3 rounded-md flex items-center text-sm font-medium";

    inActiveLinkStyleForSmallResoltions =
        "group text-gray-400 hover:bg-gray-900 hover:text-white py-2 px-3 rounded-md flex items-center text-sm font-medium";
    loginuser: number;

    constructor(private router: Router, private localStorageService: LocalStorageService, private http: HttpClient, private sanitizer: DomSanitizer, private commonService:CommonService) {

        // loginuser
        const user = this.localStorageService.getLocal('user');
        let userObj = null;
        this.user = JSON.parse(this.localStorageService.getLocal('user'))
        let loggedInuserType: number;
        console.log(this.user);


    }

    get showSideMenu(): boolean {
        return this._showOverlay;
    }

    @Input()
    set showSideMenu(val: boolean) {
        this._showOverlay = val;
        // this.state === 'normal' ? (this.state = 'highlighted') : (this.state = 'normal');
    }

    ngOnInit(): void {
      
        // loginuser
        const userData:any=this.commonService.tokenDecoder();
      
        let clusterCaseChange = userData.cluster.toLowerCase();


        this.clusterType = clusterCaseChange;
        console.log(this.clusterType);
        const user = this.localStorageService.getLocal('user');
        let userObj = null;
        this.user = JSON.parse(this.localStorageService.getLocal('user'));
        let loggedInuserType: number;
     //   console.log(this.user);

        if (userData) {
            // userObj = JSON.parse(user);
            loggedInuserType = userData.user_type;
            this.loginuser = loggedInuserType;
        }
        console.log( this.loginuser ,'dmdkdm');
        
        this.billingpath = this.loginuser != 2 ? '/billing' : '/billing/brclist';

        //this.billingpath = this.user.user_type != 2 ? '/billing' : '/billing/brclist';
        this.getImageFromService();
        // NOTE:  To check logo configure
        let domainPath = window.location.href;
        let logoPath = this.user.logo_file;
        console.log(domainPath);

        if (domainPath.includes('cm')) {
            console.log('includes cm');

            this.LOGO = '/cm' + logoPath;
        } else {
            console.log('default');
            this.LOGO = logoPath;
        }

        // NOTE:  To handle page refresh
        this.router.events.subscribe((event: any) => {
            if (event instanceof NavigationEnd) {
                this.activeLink = this.router.url.replace("/", "");
                this.activeLink = this.activeLink.split("/")[0];
                //   console.log(this.activeLink);

            }
        });
        
    }

    closeSidebarNav() {
        this.hideOverlayEvent.emit(false);
    }

    billingRoute() {

        //console.log(this.billingpath);



        this.router.navigate([this.billingpath]);

        this.closeSidebarNav();

    }
    thumbnail: any;
    // findImg() {
    //     this.getImg().subscribe((res: any) => {
    //         console.log("findImgsuccess");
    //         this.thumbnail = res
    //     }, (err) => {
    //         console.log("findImgErr");
    //         console.log(err);

    //     })
    // }

    // getImg() {
    //     let params = new HttpParams();
    //     // params = params.append('token', userDetail.token);
    //     // params = params.append('deviceId', userDetail.deviceID);
    //     // params = params.append('licenseKey', userDetail.licenseKey);

    //     const url = CONSTANTS_URL.imgUrl;
    //     return this.http.get(url, { params: params, responseType: 'blob' }).pipe(
    //         switchMap(res => {
    //             console.log(res);
    //             return this.getBlobData(res);

    //         }),
    //         catchError(error => error)
    //     );

    // }

    // getBlobData(file) {
    //     return from(this.getBlobFromFile(file)).pipe(
    //         map(data => {
    //             return {
    //                 data
    //             };
    //         })
    //     );
    // }
    // getBlobFromFile(file): Promise<any> {
    //     return new Promise((resolve, reject) => {
    //         let fileReader = new FileReader();
    //         if (file instanceof Blob) {
    //             const realFileReader = (fileReader as any)._realReader;
    //             if (realFileReader) {
    //                 fileReader = realFileReader;
    //             }
    //         }
    //         fileReader.onloadend = function () {
    //             resolve(this.result);
    //         };
    //         fileReader.onerror = function (err) {
    //             reject(err);
    //         };
    //         fileReader.readAsDataURL(file);
    //     });
    // }

    // CU-371 
    getImg(): Observable<Blob> {
        return this.http.get(this.user.logo_file, { responseType: 'blob' });
    }
    createImageFromBlob(image: Blob) {
        let reader = new FileReader();
        reader.addEventListener("load", () => {
            this.thumbnail = reader.result;
            console.log(this.thumbnail);

        }, false);

        if (image) {
            reader.readAsDataURL(image);
        }
    }
    getImageFromService() {
        this.getImg().subscribe(data => {
            this.createImageFromBlob(data);
        }, error => {
            console.log(error);
        });
    }
}
