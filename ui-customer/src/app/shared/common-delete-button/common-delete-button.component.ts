import { TextAttribute } from "@angular/compiler/src/render3/r3_ast";
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from "@angular/core";
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";


import { ActivatedRoute, Router } from "@angular/router";
import { openClose } from "../animation";
@Component({
  selector: 'app-common-delete-button',
  templateUrl: './common-delete-button.component.html',
  styleUrls: ['./common-delete-button.component.css'],
  animations: [openClose]
})
export class CommonDeleteButtonComponent implements OnInit, OnChanges {

  constructor(private route: ActivatedRoute, private router: Router) { }

  @Input() confirmationMessage: any;

  @Input() deleteListName: any;

  @Input() deletingLoader: any;

  @Input() hideshow: any;

  myOptions = value;
  
  showDeleteModal: boolean = false;

  @Output() showModal = new EventEmitter<boolean>();


  @Output() deleteModalResponse = new EventEmitter<string>();

  //   @Input() openModal: string;

  ngOnInit(): void {

  }

  onDelete() {

    this.showDeleteModal = true;
    this.hideshow == false;
    this.showModal.emit(this.hideshow)
  }

  ngOnChanges(changes: SimpleChanges): void {
    // console.log(this.deletingLoader);
    if (this.hideshow == true) {
      this.showDeleteModal = false;
    }
    // console.log(this.hideshow);


    // throw new Error("Method not implemented.");
  }


  closeDeleteModal() {
    // this.deleteModalResponse.emit('no')

    this.showDeleteModal = false;

    //this.showModal.emit(this.showDeleteModal)

  }

  deletemodal(event) {

    if (event == "no") {
      this.showDeleteModal = false;
    }
    else {
      this.showDeleteModal = true;
    }

    this.deleteModalResponse.emit(event)
    // this.showDeleteModal = false;
    // this.deleteModalResponse.emit("yes")
  }

}
