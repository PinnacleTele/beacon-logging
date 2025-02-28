import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from "@angular/router";

@Component({
  selector: 'app-nodatacreatenew',
  templateUrl: './nodatacreatenew.component.html',
  styleUrls: ['./nodatacreatenew.component.css']
})
export class NodatacreatenewComponent implements OnInit {

  @Input() title:string;

  @Input() routePath:string;

  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
  }

  createNew(){
    this.router.navigate([this.routePath], { relativeTo: this.route });
  }

}
