import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { openClose } from 'src/app/shared/animation';

@Component({
  selector: 'app-response-popup',
  templateUrl: './response-popup.component.html',
  styleUrls: ['./response-popup.component.css'],
  animations: [openClose]
})
export class ResponsePopupComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  isGroupValid: string;

  @Output() closePopUp = new EventEmitter<boolean>();

  @Output() successPopUp = new EventEmitter<{ bool: boolean, type: string }>();

  @Output() tryAgainPopUp = new EventEmitter<{ bool: boolean, type: string }>();

  @Input() status: string;


  @Input() Responce: { message: string, statusCode: number, error?: string }

  message: any;

  hideCancel: boolean = false;

  @Input() currentCall = ""

  // get response(){
  //     let data:string
  //         if(this.Responce?.statusCode>199 && this.Responce?.statusCode<300){
  //         data="success"
  //         }
  //         else{
  //            data="failure"
  //         }
  //         return data

  // }


  get typeOfResponce() {
    if (this.Responce.error == 'entityId') {
      this.hideCancel = true;
    }
    this.message = this.Responce.message;
    // console.log(this.Responce.statusCode);
    return this.Responce.statusCode
  }

  closeCreateModal() {
    // if(this.Responce.statusCode!==201)
    //  console.log("closepops");

    this.closePopUp.emit(true);
  }

  modalContinue() {
    this.successPopUp.emit({ bool: false, type: this.currentCall });
  }

  tryAgain() {
    this.tryAgainPopUp.emit({ bool: false, type: this.currentCall });
    // this.closeCreateModal();
  }
}
