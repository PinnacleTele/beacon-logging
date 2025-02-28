import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { openClose } from '../../animation';
@Component({
  selector: 'app-delete-confirmation-popup',
  templateUrl: './delete-confirmation-popup.component.html',
  styleUrls: ['./delete-confirmation-popup.component.css'],
  animations:[openClose]
})
export class DeleteConfirmationPopupComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
  @Input()isShow=false;
  @Input() showDeleteModal=false
  @Input()  confirmationMessage="are you sure want to delete this campaign"
  @Output() onYes =new EventEmitter<boolean>()

  onClickYes(){
this.onYes.emit(true)

  }
  onClickNo(){
    this.onYes.emit(false)
  }
  closeDeleteModal(){
    this.showDeleteModal=false
  }
}
