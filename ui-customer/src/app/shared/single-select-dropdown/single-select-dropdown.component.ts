import {
    Component,
    EventEmitter,
    Input,
    OnInit,
    Output,
    ViewChild,
    AfterViewChecked
} from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { NgSelectComponent } from "@ng-select/ng-select";

@Component({
    selector: "app-single-select-dropdown",
    templateUrl: "./single-select-dropdown.component.html",
    styleUrls: ["./single-select-dropdown.component.css"]
})
export class SingleSelectDropdownComponent implements OnInit, AfterViewChecked {
    constructor(private fb: FormBuilder) {}

    @Input() required: boolean;

    itemForm: FormGroup;

    @ViewChild("select") select: NgSelectComponent;

    @ViewChild(SingleSelectDropdownComponent, { static: false })
    selectid: SingleSelectDropdownComponent;

    ngOnInit(): void {
        this.itemForm = this.fb.group({
            ItemSelected: ["", this.required ? Validators.required : null]
        });
        
    }

    get ItemSelected() {
        return this.itemForm.get("ItemSelected");
    }

    ngAfterViewChecked(): void {
       // this.directive.setngselect(this.selectid);
       // console.log(this.itemToBeSelected);
    }

    @Input() itemsList: any;

    @Output() selectedItem = new EventEmitter();

    @Input() placeHolder: string;

    @Input() itemToBeSelected: string = "Asia/Dubai";

    // itemToBeSelect: any = "Asia/Dubai";

    @Input() errorMessage: string;

    @Input() infoMessage: string;

    onItemChange(event: any) {
        this.selectedItem.emit(event);
    }

    validateSelectBox() {
        this.itemForm.get("ItemSelected")?.markAsTouched();
    }

    setfocus() {
        this.select.focus();
    }
}
