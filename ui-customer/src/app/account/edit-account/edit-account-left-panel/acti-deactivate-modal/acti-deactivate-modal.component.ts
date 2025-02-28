import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Subscription } from 'rxjs';
import { AccountService } from 'src/app/account/account.service';

@Component({
  selector: 'app-acti-deactivate-modal',
  templateUrl: './acti-deactivate-modal.component.html',
  styleUrls: ['./acti-deactivate-modal.component.css']
})
export class ActiDeactivateModalComponent implements OnInit {
  response:{message:string,statusCode:number};
  status: string;
  popup = false;
  spinner = false;
  disabled = false;
showModal = true;
@Input() action = "";
cliId = "";
@Output() close = new EventEmitter();
@Output() chgStatus = new EventEmitter<string>();
subscription : Subscription;
  constructor(private route: ActivatedRoute ,
    private accountService : AccountService) { }

  ngOnInit(): void {
    this.subscription = this.route.queryParams.subscribe(
      params => {
        this.cliId = params["accounts"];
      });
  }
 
  modalClose() {
    this.close.emit();
    this.popup = false;
    
  }


  tryAgain(event: any) {
   this.popup=false
   this.acctStatusUpdate(this.action);
     
  }

  modalcontinue(event: boolean) {
    this.chgStatus.emit(this.action);
    this.popup = false;
  }
  

  acctStatusUpdate(action : string){
    this.spinner = true;
    this.disabled = true;
    
    
    this.accountService.updateAcctStatus(this.cliId, action).subscribe(
      (res: any) => {
        
        this.spinner = false;
        this.disabled = false;
        this.popup = true;
        this.response = res;
        this.showModal = false;
        
      },
      (error: HttpErrorResponse) => {
        
        this.disabled = true;
        
        this.spinner = false;
        let err =this.accountService.badError
        this.response = err;
        this.status=error.message
        this.popup=true;
      });
  }

}
