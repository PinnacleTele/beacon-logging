import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-nodataerror',
  templateUrl: './nodataerror.component.html',
  styleUrls: ['./nodataerror.component.css']
})
export class NodataerrorComponent implements OnInit {

  @Input() title:string;

  constructor() { }

  ngOnInit(): void {
    
  }

}
