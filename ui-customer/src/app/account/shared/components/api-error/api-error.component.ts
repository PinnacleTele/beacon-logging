import { Component, EventEmitter, OnInit, Output } from '@angular/core';


@Component({
  selector: 'app-api-error',
  templateUrl: './api-error.component.html',
  styleUrls: ['./api-error.component.css']
})
export class ApiErrorComponent implements OnInit {
  @Output() tryAgain = new EventEmitter();
  constructor() { }

  ngOnInit(): void {
  }
  onClickTryAgain(){
    this.tryAgain.emit();
  }

}
