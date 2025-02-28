import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ControlContainer, FormControl, FormGroup } from '@angular/forms';

@Component({
  selector: 'app-toggle-button',
  templateUrl: './toggle-button.component.html',
  styleUrls: ['./toggle-button.component.css']
})
export class ToggleButtonComponent implements OnInit {
  enableButton = false;
  toggleFormGroup : any;
  @Input() formGroup : FormGroup;
  @Input() controlName : string;
  @Input() toggle : boolean;
  @Output() toggled = new EventEmitter();

  constructor( private controlContainer: ControlContainer){ }
  
  ngOnInit(): void {
    //console.log(this.controlName);
    
    this.toggleFormGroup = this.controlContainer.control;
    this.enableButton = this.name.value;
  }

  onClickToggle(event : any){
    
    this.enableButton = !this.enableButton;
    this.name.setValue(this.enableButton);
    this.toggled.emit();
  }

  
  get name() {
    const control = this.formGroup.get(this.controlName);
    
    
    return control as FormControl;
  }

}
