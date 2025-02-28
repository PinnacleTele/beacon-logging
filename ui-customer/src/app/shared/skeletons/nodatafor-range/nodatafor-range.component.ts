import { Component, Input, OnInit ,OnChanges, SimpleChanges} from '@angular/core';

@Component({
  selector: 'app-nodatafor-range',
  templateUrl: './nodatafor-range.component.html',
  styleUrls: ['./nodatafor-range.component.css']
})
export class NodataforRangeComponent implements OnInit,OnChanges {

  @Input() messageContent:any;

  @Input() headContent:any;

  @Input() title:string;

  constructor() { }
  ngOnChanges(changes: SimpleChanges): void {
   
  }

  ngOnInit(): void {
  }

}
