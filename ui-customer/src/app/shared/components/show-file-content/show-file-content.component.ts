import { Component, EventEmitter, Input, Output, SimpleChanges } from "@angular/core";

@Component({
    selector: "app-show-file-content",
    templateUrl: "./show-file-content.component.html",
    styleUrls: ["./show-file-content.component.css"]
})
export class ShowFileContentComponent {
    showFileContents = false;
    @Input() fileUploaded : any;
    @Output() showFilePreview = new EventEmitter<boolean>();
    ngOnChanges(changes: SimpleChanges): void {
        if(!this.fileUploaded){
            this.showFileContents = !this.showFileContents;
        }
    }
    toggleFileContent() {
        this.showFileContents = !this.showFileContents;
        
        this.showFilePreview.emit(this.showFileContents);
    }
}
