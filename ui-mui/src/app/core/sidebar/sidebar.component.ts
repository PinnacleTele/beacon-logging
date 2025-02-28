import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";

@Component({
    selector: "app-sidebar",
    templateUrl: "./sidebar.component.html",
    styleUrls: ["./sidebar.component.css"],
    
})
export class SidebarComponent implements OnInit {
    _showOverlay = false;

    @Output() hideOverlayEvent: EventEmitter<boolean> = new EventEmitter();

    public LOGO :any;
    state = "normal";

    @Input() activeLink = "";

    user:any;

    activeLinkStyle =
    "bg-gray-900 text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

    inActiveLinkStyle =
    "group text-gray-400 hover:bg-gray-900 hover:text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

    activeLinkStyleForSmallResoltions =
        "bg-gray-900 text-white py-2 px-3 rounded-md flex items-center text-sm font-medium";

    inActiveLinkStyleForSmallResoltions =
        "group text-gray-100 hover:bg-gray-800 hover:text-white py-2 px-3 rounded-md flex items-center text-sm font-medium";
    

    constructor(private router: Router) {

        
       
        
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

        // NOTE:  To check logo configure
        let domainPath = window.location.href;
        let logoPath = "/assets/logo/default_logo.png";

        if (domainPath.includes('cm')) {
            
            this.LOGO = '/cm' + logoPath;
        }else{
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
}
