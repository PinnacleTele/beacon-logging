import {
    Directive,
    ElementRef,
    Renderer2,
    OnInit,
    Optional,
    OnChanges,
    Output,
    EventEmitter,
    HostListener,
} from "@angular/core";
@Directive({
    selector: "[focus-out-directive]"
 })
 export class FocusOutDirective {
    @Output() onFocusOut: EventEmitter<boolean> = new EventEmitter<false>();
 
    @HostListener("focusout", ["$event"])

    public onListenerTriggered(event: any): void {


        // this.onFocusOut.emit(true);
    }
 }