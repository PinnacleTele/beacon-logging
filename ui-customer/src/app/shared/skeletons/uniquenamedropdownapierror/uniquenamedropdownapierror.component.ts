import { Component, EventEmitter, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-uniquenamedropdownapierror',
  templateUrl: './uniquenamedropdownapierror.component.html',
  styleUrls: ['./uniquenamedropdownapierror.component.css']
})
export class UniquenamedropdownapierrorComponent implements OnInit {

  @Output() Emitter = new EventEmitter<any>();

  constructor() { }

  ngOnInit(): void {
  }

  retry(){
    this.Emitter.emit("retry")
  }
}
