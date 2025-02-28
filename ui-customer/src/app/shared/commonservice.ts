import { Injectable, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthLoginService } from '../authentication/auth-login.service';
import { LocalStorageService } from '../authentication/local-storage.service';

@Injectable({
  providedIn: 'root'
})
export class CommonService implements OnInit {

    userZone:string="";

    userDetail:any;

   

  constructor(private localStorageService : LocalStorageService,private router:Router,private authService:AuthLoginService){
  
  }
    ngOnInit(): void {
      
     //   throw new Error('Method not implemented.');
    
    }

 getUserData(){
     
  let userData = this.localStorageService.getLocal('user');
  let jsonData = JSON.parse(userData);
  this.userZone = jsonData.zone_abbr;
 
  return this.userZone;
 }

 getUserdetail(){
  let userData =this.localStorageService.getLocal('user');
  let jsonData = JSON.parse(userData);
  this.userDetail = jsonData;
   return this.userDetail;
 }

 selectedDates: Map<string, any> = new Map<string, any>();
  

 
 finalItem:any[]=[]

 customSerach(arrayList:any,dynamicValue:any,selectedValue:any){
     
     const temparray = [];
    if (selectedValue != undefined) {
         arrayList.filter((data: any) => {
             if (data[dynamicValue].includes(selectedValue)) {
                 temparray.push(data);
                 this.finalItem = temparray;
             }
         })
     } else {
         this.finalItem = arrayList;
     }
     
    // console.log(this.finalItem,'final array');
     return this.finalItem;
 }
 parseJwt(token) {
    var base64Url = token.split('.')[1];
    var base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    var jsonPayload = decodeURIComponent(
        atob(base64)
            .split('')
            .map(function (c) {
                return (
                    '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
                );
            })
            .join('')
    );

    return JSON.parse(jsonPayload);
}

tokenDecoder(){
    try{
        let accessTokenDetail=localStorage.getItem('token')
        return this.parseJwt(accessTokenDetail);
    }
    catch(Exception ){
         this.authService.logoutInvalidSess();       
    }
}
}