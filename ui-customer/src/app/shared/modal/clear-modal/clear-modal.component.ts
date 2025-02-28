import { Component, EventEmitter, Input, Output } from "@angular/core";
import { openClose } from "../../animation";

@Component({
    selector: "app-clear-modal",
    templateUrl: "./clear-modal.component.html",
    styleUrls: ["./clear-modal.component.css"],
    animations:[openClose]
})
export class ClearModalComponent {
    @Input() whichTextArea: string;

    @Input() title: string;

    @Input() confirmationMessage: string;

    @Input() confirmLable: string;

   @Input() showClearModal = true;

    @Output() clearModalResponse = new EventEmitter<string>();

    closeClearModal() {
        this.clearModalResponse.emit("close");
        this.showClearModal = false;
    }

    onConfirmClear() {
        this.clearModalResponse.emit("clear");
    }
}
