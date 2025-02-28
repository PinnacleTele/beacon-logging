import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-apierror',
  templateUrl: './apierror.component.html',
  styleUrls: ['./apierror.component.css']
})
export class ApierrorComponent implements OnInit {

  @Output() Emitter = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
    console.log("mame");

  }


  Retry() {
    this.Emitter.emit("retry")
  }
}
