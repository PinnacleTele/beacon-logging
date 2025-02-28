import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';

@Component({
  selector: 'app-sorter',
  templateUrl: './sorter.component.html',
  styleUrls: ['./sorter.component.css']
})
export class SorterComponent implements OnInit {

  constructor() { }


  ngOnInit(): void {
  }
  @Input() icon = 0;
  @Input() sortingProperty = "";
  @Input() colTitle = "";
  @Input() defaultProperty = ""
  @Input() defaultOrder = ""
  @Output() sortingEmitter = new EventEmitter();
  @Output() iconEmitter = new EventEmitter();
  @Output() oneEmitter = new EventEmitter();

  onAsending() {
    this.icon = 1;
    this.iconEmitter.emit({ prop: this.sortingProperty, icon: this.icon })
    this.sortingEmitter.emit({ prop: this.sortingProperty, order: "asc" })
  }
  onDefault() {
    this.icon = 0;
    this.iconEmitter.emit({ prop: this.sortingProperty, icon: this.icon })
    this.sortingEmitter.emit({ prop: this.defaultProperty, order: this.defaultOrder })
  }
  ondecending() {
    this.icon = 2;
    this.iconEmitter.emit({ prop: this.sortingProperty, icon: this.icon })
    this.sortingEmitter.emit({ prop: this.sortingProperty, order: "dce" })
  }

  sort() {

    if (this.icon == 0 || this.icon == 3) {
      this.onAsending();
    }
    else if (this.icon == 1) {
      this.ondecending();

    }
    else if (this.icon == 2) {
      this.onDefault();
    }

  }
}
