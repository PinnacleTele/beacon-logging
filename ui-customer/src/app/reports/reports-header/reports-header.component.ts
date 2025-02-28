import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'app-reports-header',
  templateUrl: './reports-header.component.html',
  styleUrls: ['./reports-header.component.css']
})
export class ReportsHeaderComponent implements OnInit {

  constructor(private router:Router) { }
  activeLink=""

  ngOnInit(): void {
   // this.router.navigate(["reports/summary"])
   
   
 
  let arr=this.router.url.split("/")
  this.activeLink=arr[arr.length-1]
  }
  activeLinkStyle =
  "bg-indigo-800 text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

inActiveLinkStyle =
  "group text-indigo-100 hover:bg-indigo-800 hover:text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";


}
