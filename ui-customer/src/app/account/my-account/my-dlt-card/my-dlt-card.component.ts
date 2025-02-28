import { Component, Input, OnInit, SimpleChanges } from '@angular/core';
import { TemplateGroup } from '../../shared/model/template-group-model';

@Component({
	selector: 'app-my-dlt-card',
	templateUrl: './my-dlt-card.component.html',
	styleUrls: ['./my-dlt-card.component.css']
})
export class MyDltCardComponent implements OnInit {
	@Input() allocTG: TemplateGroup[] = [];
	@Input() selectedAcctType = '';
	allocatedGroups: TemplateGroup[] = [];
	@Input() assignedTGName: string;

	constructor() {}

	ngOnInit(): void {
		this.allocatedGroups = this.allocTG;
	}
	ngOnChanges(changes: SimpleChanges): void {
		this.allocatedGroups = changes.allocTG.currentValue;
		this.assignedTGName = changes.assignedTGName.currentValue;
		this.selectedAcctType = changes.selectedAcctType.currentValue;
	}
}
