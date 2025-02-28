import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';

@Component({
  selector: 'app-reports',
  templateUrl: './reports.component.html',
  styleUrls: ['./reports.component.css']
})
export class ReportsComponent implements OnInit {

  constructor(private router:Router) { 
    
  }
  activeLink="summary"
  
  ngOnInit(): void {
    

  }

  activeLinkStyle =
  "bg-indigo-800 text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";

inActiveLinkStyle =
  "group text-indigo-100 hover:bg-indigo-800 hover:text-white w-full p-3 rounded-md flex flex-col items-center text-xs font-medium";




}
