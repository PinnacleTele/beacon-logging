import {  Component, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-frame',
  templateUrl: './frame.component.html',
  styleUrls: ['./frame.component.css']
})
export class FrameComponent implements OnInit {
  title = "SmsPortal";
  showOverlay = true;
  isLoggedIn = true;
  

  
  
  constructor(
    private router: Router) { }

  

  ngOnInit(): void {
  
      
    
   
  }
  overlayEventHandler(p: any) {
    this.showOverlay = p;
    // console.log(`showOverlay --- `+this.showOverlay);
  }


}
