import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { openClose } from '../animation';

@Component({
  selector: 'app-deletemodal',
  templateUrl: './deletemodal.component.html',
  styleUrls: ['./deletemodal.component.css'],
  animations: [openClose]
})
export class DeletemodalComponent implements OnInit,OnChanges {

  @Input() confirmationMessage: any;

  @Input() deleteListName: any;

  @Input() deletingLoader: any;

  action = "yes"


   @Input() showDeleteModal: boolean = false;

  @Output() deleteModalResponse = new EventEmitter<string>();


  constructor() { }

  ngOnChanges(changes: SimpleChanges): void {

    if (this.deletingLoader == true) {
      this.action = 'Deleting'
    }
    else if(this.deletingLoader == false){
      this.action = 'Yes';
    
    }
  
  }

  ngOnInit(): void {
  }

  closeDeleteModal() {
    this.deleteModalResponse.emit('no')
    //this.showDeleteModal = false;

  }

  onClickYes() {

    this.deleteModalResponse.emit('yes')
   // this.showDeleteModal = false;
    // this.deleteModalResponse.emit("yes")
  }
}
