import { Component } from "@angular/core";
import { ControlContainer } from "@angular/forms";
import { CONSTANTS, value } from 'src/app/shared/campaigns.constants';

@Component({
    selector: "app-remove-duplicates",
    templateUrl: "./remove-duplicates.component.html",
    styleUrls: ["./remove-duplicates.component.css"]
})
export class RemoveDuplicatesComponent {
    removeDuplicateForm: any;

    myOptions = value;

    constructor(private controlContainer: ControlContainer) {}

    ngOnInit(): void {
        this.removeDuplicateForm = this.controlContainer.control;
    }

    get senderId() {
        return this.removeDuplicateForm.get("removeDuplicates");
    }

    onChange(event: any) {
        
        const duplicates = event.target.checked;

        this.removeDuplicateForm.controls.removeDuplicates.setValue(duplicates);
    }
}
