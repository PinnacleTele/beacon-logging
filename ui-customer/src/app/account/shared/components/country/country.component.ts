import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { ControlContainer } from '@angular/forms';
import { NgSelectComponent } from '@ng-select/ng-select';
import { ACCT_CONSTANTS } from 'src/app/account/account.constants';
import { EditAccountService } from 'src/app/account/edit-account/edit-account.service';
import { CONSTANTS } from 'src/app/shared/campaigns.constants';
import { AccountService } from '../../../account.service';
import { Country } from '../../model/country-model';


@Component({
  selector: 'app-country',
  templateUrl: './country.component.html',
  styleUrls: ['./country.component.css']
})
export class CountryComponent implements OnInit {
  
  itemToBeSelected = "";
  @Input() discarded : boolean;
  @Input() formChanged : any
  @Input() itemList : any
  @Output() countrySelected = new EventEmitter();
  @ViewChild(NgSelectComponent) ngSelectComponent: NgSelectComponent;
  response:{message:string,statusCode:number};
  countryInfoText = ACCT_CONSTANTS.INFO_TXT.country;
  status: string;
  popup = false;
  
  countryFormGroup: any;
  spinner = false;
  
  constructor(private accountService : AccountService,
    private controlContainer: ControlContainer,
    private editAcctSvc : EditAccountService) { }

  ngOnInit(): void {
    
    this.countryFormGroup = this.controlContainer.control;
    this.itemToBeSelected = CONSTANTS.COUNTRY;
   // this.getCountryList();
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.itemList = this.itemList;
  }

  getCountryList(){
    
    this.spinner =true;
    this.popup = false;
    let selectedCountries = this.accountService.selectedCountries ;
    console.log(selectedCountries)
    this.countryFormGroup.controls.country.setErrors({
      chkServerError: true });
    this.accountService.getAllCountries().subscribe(

     (res: any) => {
      this.countryFormGroup.controls.country.setErrors(null);
         this.spinner = false;
         const countries: Country[] = [];
         
             res.forEach((ele:any) => {
                 //countries.push(ele);
                let iso_3 = ele.country_code_iso_3;
                let countryName = ele.country;
                if(countryName.length != 0){
                  countryName = " (" +countryName+ ")"
                }
                countries.push({ country_code_iso_3 :iso_3, country :iso_3+countryName})
             });
          //   this.itemList = countries;
             if(selectedCountries.length > 0){
               console.log("here1")
             this.itemList  = countries.filter(function (el) {
                console.log(selectedCountries.indexOf(el.country_code_iso_3) )
                return selectedCountries.indexOf(el.country_code_iso_3) == -1
               })
             }
            
      

            
     },
     (error: HttpErrorResponse) => {
      this.countryFormGroup.controls.country.setErrors({
        apiRequestError: true });
      this.popup = true;
      this.spinner = false;
      
     }

 );
}

get country() {
  return this.countryFormGroup.get("country");
}

handleRetryClick(){
    this.getCountryList();
}
getSelectedCountry(event : any){

  if(event){
    this.accountService.selectedCountries.push(event.country_code_iso_3)
    this.countrySelected.emit(event.country_code_iso_3);
  }
  
}

}

