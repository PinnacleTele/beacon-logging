import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { emit } from 'process';

@Component({
  selector: 'app-retry',
  templateUrl: './retry.component.html',
  styleUrls: ['./retry.component.css']
})
export class RetryComponent implements OnInit {
@Output() retry = new EventEmitter<null>();
  constructor() { }

  ngOnInit(): void {
  }
  retryClicked(){
      this.retry.emit();
  }

}
