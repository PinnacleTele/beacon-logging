import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { openClose } from '../../animation';

@Component({
  selector: 'app-delete-res-popup',
  templateUrl: './delete-res-popup.component.html',
  styleUrls: ['./delete-res-popup.component.css'],
  animations: [openClose]
})
export class DeleteResPopupComponent implements OnInit {
  ngOnInit(): void {
    

  }

  isGroupValid: string;

  @Output() closePopUp = new EventEmitter<boolean>();

  @Output() successPopUp = new EventEmitter<boolean>();

  @Output() tryAgainPopUp = new EventEmitter<boolean>();

  @Input() status: string;


  @Input() Responce: { message: string, statusCode: number, error?: string }

  message: any;

  hideCancel: boolean = false;

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
    this.successPopUp.emit(true);
  }

  tryAgain() {
    this.tryAgainPopUp.emit(true);
    // this.closeCreateModal();
  }
}
