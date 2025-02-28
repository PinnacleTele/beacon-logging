import { AfterViewChecked, Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthLoginService } from 'src/app/authentication/auth-login.service';
import { LocalStorageService } from 'src/app/authentication/local-storage.service';
import { HeaderComponent } from 'src/app/core/header/header.component';
import { DownloadsService  } from "src/app/downloads/Helpers/downloads-service.service";
@Component({
  selector: 'app-frame',
  templateUrl: './frame.component.html',
  styleUrls: ['./frame.component.css']
})
export class FrameComponent implements OnInit ,AfterViewChecked{
  title = "SmsPortal";
  showOverlay = false;
  isLoggedIn = false;
  activeLink=this.router.url.split("/")[1];
  //isLoggedIn$: Observable<boolean>;


  @ViewChild(HeaderComponent, { static: false })
  header: HeaderComponent;

  constructor(private localStorage :  LocalStorageService,
    private downloadService: DownloadsService,
    private router: Router) { }

  ngAfterViewChecked(): void {
    this.downloadService.setHeaderDownloadControl(this.header);
   // throw new Error('Method not implemented.');
  }

  ngOnInit(): void {
    const user = this.localStorage.getLocal("user");
    if( user != null && user != ""){
      this.isLoggedIn = true;
    }else{
      this.isLoggedIn = false;
      this.router.navigate(["/login"]);
    }
   
  }
  overlayEventHandler(p: any) {
    this.showOverlay = p;
    // console.log(`showOverlay --- `+this.showOverlay);
  }


}
