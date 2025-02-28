import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-file-pagination',
  templateUrl: './file-pagination.component.html',
  styleUrls: ['./file-pagination.component.css']
})
export class FilePaginationComponent implements OnInit,OnChanges {

  @Input() total: number ;

  @Input() totalRecord: number ;
  
  @Input() p: number = 1;
  
  @Input() pagesize: number;
  
  @Input() itemsPerPage: number = 10;
  
  @Input() perpageCount: number = 10;

  @Input() tableData: any;

  @Input() searchTextLength: any;

  @Output() paginationValueEmit = new EventEmitter<any>();

  // @Input() tableData: any;





  constructor() { }
  
  ngOnChanges(changes: SimpleChanges): void {
   // throw new Error('Method not implemented.');
  }

  ngOnInit(): void {
    
  }

  get startItem() {
    if (this.total == 0) {
     return ((this.p - 1) * this.itemsPerPage) ;
    }else  if (this.total < this.itemsPerPage){
     return this.p = 1;
   }else{    
     return ((this.p - 1) * this.itemsPerPage) + 1;
   }
 }

 get endItem() {
   
  var end = this.p * this.itemsPerPage;
  return end < this.total ? end : this.total;
}

 get isFirstPage() {
   return this.p == 1 ? true : false;
 }

 get isLastPage() {
   return this.endItem == this.total ? true : false;
 }

//  next click event
next(event: any) {
event.preventDefault();
 this.p += 1;

 this.paginationValueEmit.emit(this.p)
}

// -----previous click event
prev(event: any) {
 event.preventDefault();
 this.p -= 1;
 this.paginationValueEmit.emit(this.p)

}
}
