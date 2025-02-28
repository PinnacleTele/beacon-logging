import { THIS_EXPR } from "@angular/compiler/src/output/output_ast";
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";

@Component({
    selector: "app-create-popup",
    templateUrl: "./create-popup.component.html",
    styleUrls: ["./create-popup.component.css"]
})
export class CreatePopupComponent implements OnInit {
    

    ngOnInit(): void {
        // this.isGroupValid =
        //     this.status === "success"
        //         ? "has been Created Successfully "
        //         : " has some Errors";
        
        
    }

    isGroupValid: string;

    @Output() closePopUp = new EventEmitter<boolean>();

    @Output() successPopUp = new EventEmitter<boolean>();

    @Output() tryAgainPopUp = new EventEmitter<boolean>();

    @Input() status: string;

    @Input() inputForm: any;

    @Input() Responce:{message:string,statusCode:number,error?:string}

    message : any;

    hideCancel : boolean = false;

// get response(){
//     let data:string
//         if(this.Responce?.statusCode>199 && this.Responce?.statusCode<300){
//         data="success"
//         }
//         else{
//            data="failure"
//         }
//         return data
    
// }
    get name() {        
        return this.inputForm?.value?.name;
    }

    get typeOfResponce(){
    if (this.Responce.error == 'entityId') {
        this. hideCancel = true;
    }
        this.message = this.Responce.message;
       // console.log(this.Responce.statusCode);
        return this.Responce.statusCode
    }

    closeCreateModal() {
        if(this.Responce.statusCode!==201)
       this.closePopUp.emit(true);
    }

    modalContinue() {
        this.successPopUp.emit(true);
    }

    tryAgain() {
        this.tryAgainPopUp.emit(true);
        // this.closeCreateModal();
    }
}
