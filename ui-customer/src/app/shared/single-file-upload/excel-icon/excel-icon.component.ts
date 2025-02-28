import { Component, Input, OnInit } from '@angular/core';
import { CONSTANTS, value} from "src/app/shared/campaigns.constants";

@Component({
  selector: 'app-excel-icon',
  templateUrl: './excel-icon.component.html',
  styleUrls: ['./excel-icon.component.css']
})
export class ExcelIconComponent implements OnInit {
@Input() fileName : any;

myOptions = value;
  constructor() { }

  ngOnInit(): void {
  }

}
