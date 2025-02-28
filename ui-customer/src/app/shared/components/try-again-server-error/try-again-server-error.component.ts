import { Component, EventEmitter, OnInit, Output } from '@angular/core';


@Component({
  selector: 'app-try-again-server-error',
  templateUrl: './try-again-server-error.component.html',
  styleUrls: ['./try-again-server-error.component.css']
})
export class TryAgainServerErrorComponent implements OnInit {
@Output() tryClicked = new EventEmitter();
  constructor() { }

  ngOnInit(): void {
  }

  onClickTry(){
    this.tryClicked.emit();
  }

}
