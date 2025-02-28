import { Component, OnInit } from '@angular/core';
import { DltUploadService  } from "src/app/dltfile-upload/dltupload.service";
@Component({
  selector: 'app-dltfile-upload',
  templateUrl: './dltfile-upload.component.html',
  styleUrls: ['./dltfile-upload.component.css']
})
export class DltfileUploadComponent implements OnInit {

  constructor(private dltservice:DltUploadService) { }

  ngOnInit(): void {
    this.telcoSubscribe();
  }

telcoList:any[] = []

  telcoSubscribe(){
    this.dltservice.getTelco().subscribe((data:any)=>{
      this.telcoList =  data;
      
    })
  }
}
