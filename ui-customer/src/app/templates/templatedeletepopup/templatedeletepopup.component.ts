import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { openClose } from 'src/app/shared/animation';

@Component({
  selector: 'app-templatedeletepopup',
  templateUrl: './templatedeletepopup.component.html',
  styleUrls: ['./templatedeletepopup.component.css'],
  animations:[openClose]
})
export class TemplatedeletepopupComponent implements OnInit {

  constructor() { }

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

  get typeOfResponce() {
    this.message = this.Responce.message;
    return this.Responce.statusCode
  }

  closeCreateModal() {
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
