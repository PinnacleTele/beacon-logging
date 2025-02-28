import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-download-skeleton',
  templateUrl: './download-skeleton.component.html',
  styleUrls: ['./download-skeleton.component.css']
})
export class DownloadSkeletonComponent implements OnInit {

  constructor() { }

  ngOnInit(): void {
  }

  array=[1,2,3,4,5,6,7,8,9,1]

}
