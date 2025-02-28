import { newArray } from '@angular/compiler/src/util';
import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-list-skeleton',
  templateUrl: './list-skeleton.component.html',
  styleUrls: ['./list-skeleton.component.css']
})
export class ListSkeletonComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }
items=[1,2,3,4,5,6,7,8,9,10]
@Input()colCount:number
coloums(){
  return new Array(this.colCount)
}
}
