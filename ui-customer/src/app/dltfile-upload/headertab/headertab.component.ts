import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-headertab',
  templateUrl: './headertab.component.html',
  styleUrls: ['./headertab.component.css']
})
export class HeadertabComponent implements OnInit {

  @Input() routerAction:any;

  constructor() { }

  ngOnInit(): void {

  }

}
