import { Component, EventEmitter, Input, Output } from "@angular/core";
import { openClose,Container1 } from "../../animation";
@Component({
    selector: "app-cancel-modal",
    templateUrl: "./cancel-modal.component.html",
    styleUrls: ["./cancel-modal.component.css"],
    animations:[Container1,openClose]
})
export class CancelModalComponent {
    showCancelModal = true;

    @Input() cancelMessage: string;

    @Output() cancelModalResponse = new EventEmitter<string>();

    closeCancelModal() {
        this.cancelModalResponse.emit("close");
        this.showCancelModal = false;
    }

    onClickYes() {
        this.cancelModalResponse.emit("cancel");
    }
}
