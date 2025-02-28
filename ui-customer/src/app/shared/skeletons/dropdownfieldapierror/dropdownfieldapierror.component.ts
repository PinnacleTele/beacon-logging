import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';

@Component({
  selector: 'app-dropdownfieldapierror',
  templateUrl: './dropdownfieldapierror.component.html',
  styleUrls: ['./dropdownfieldapierror.component.css']
})
export class DropdownfieldapierrorComponent implements OnInit {

  @Output() Emitter = new EventEmitter<any>();

  
  myOptions = value;

  constructor() { }

  ngOnInit(): void {
  }


  Retry() {
    this.Emitter.emit("retry")
  }
}
