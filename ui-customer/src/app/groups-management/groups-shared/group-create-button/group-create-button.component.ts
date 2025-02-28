import { HttpErrorResponse } from '@angular/common/http';
import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { Router } from '@angular/router';
import { UtilityService } from 'src/app/core/utility.service';
import { openClose } from 'src/app/shared/animation';
import { GroupsManagementService } from '../../groups-management.service';

@Component({
    selector: 'app-group-create-button',
    templateUrl: './group-create-button.component.html',
    styleUrls: ['./group-create-button.component.css'],
    animations: [openClose]
})
export class GroupCreateButtonComponent implements OnInit, OnDestroy {

    constructor(private router: Router,
        private utility: UtilityService,
        private groupManagementService: GroupsManagementService) { }
    ngOnDestroy(): void {
        if (this.fileProgSubscriber) {
            this.fileProgSubscriber.unsubscribe();
        }

    }


    fileProgSubscriber = this.utility.fileUploadProgress.subscribe((data: any) => { this.fileOnUpload = data })

    fileOnUpload: any;

    @Input() title = ""

    spinner = false;

    popup = false;

    @Input() newGroupForm: any;

    Responce: { message: string, statusCode: number };

    status: string;

    @Input() editId: string = "";

    @Input() loading = false;
    @Input() disable = false;

    detailsFromUploader: any[] = [];

    processedFiles: any[] = [];


    ngOnInit(): void {
    }

    enable() {

        if (this.title == "Save" || this.title == "Save Contact") {
            if (this.newGroupForm.valid && this.newGroupForm.dirty) {
                return true;
            }
            else {
                return false;
            }
        }
        else if (this.title == "Create" || this.title == "Add Contact") {

            if (this.newGroupForm.valid && !this.fileOnUpload) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    @Output() nameFocus = new EventEmitter<boolean>();

    onCreate() {


        this.groupManagementService.validateAllFormFields(this.newGroupForm);
        if (this.newGroupForm.controls.name && this.newGroupForm.controls.name.errors) {
            this.nameFocus.emit(true);
        }
        if (this.newGroupForm.valid) {
            this.onSubmit();
            this.spinner = true;
        }

    }



    modalClose(event: boolean) {
        this.popup = false;
    }

    tryAgain(event: any) {
        this.onSubmit();

    }

    modalcontinue(event: boolean) {
        if (this.title == "Save") {
            this.router.navigate(["/groups"]);
        }
        else if (this.title === 'Save Contact') {
            this.router.navigate(["/groups/groupcontacts"]);
        }
        else if (this.title === "Add Contact") {
            this.router.navigate(["/groups/groupcontacts"]);
        }
        else if (this.title === "Create") {
            this.router.navigate(["/groups"]);
        }
    }

    onSubmit() {
        this.spinner = true;
        this.popup = false;

        if (this.title === 'Create') {
            this.newGroup();
        }
        else if (this.title === 'Save Contact') {
            this.editContact();

        }
        else if (this.title === "Add Contact") {
            this.addContact();
        }
        else if (this.title == "Save") {
            this.updateGroup();
        }
        //this.title=oldTitle
    }



    newGroup() {

        let Gname = this.newGroupForm.get('name').value.trim();
        //let gname = this.newGroupForm.controls.name.value;

        let name: string = Gname;
        let type: string = this.newGroupForm.controls.groupType.value;
        let visiblity: string = this.newGroupForm.controls.visibility.value;


        this.detailsFromUploader = this.newGroupForm.get('files').value
        this.processedFiles = []
        this.detailsFromUploader.forEach((element: any) => {

            let files =
            {

                "filename": element.originalname,
                "r_filename": element.r_filename,
                "count": element.total,

            }
            this.processedFiles.push(files)
        });


        this.groupManagementService.postgroup(name, type, visiblity, this.processedFiles).subscribe((responcedata: any) => {

            if (
                responcedata.statusCode === 201
            ) {
                this.Responce = responcedata
                this.status = responcedata.message
                this.spinner = false;
                this.popup = true;
            }
            if (responcedata.statusCode === -400) {
                this.Responce = responcedata
                this.status = responcedata.message
                this.spinner = false;
                this.popup = true;
            }
        },

            (error: HttpErrorResponse) => {
                let err = this.groupManagementService.badError
                this.Responce = err;
                this.status = err.message
                this.popup = true;
                this.spinner = false;

            }
        );
    }


    updateGroup() {

        let name = this.newGroupForm.get('name').value.trim();
        let visibility = this.newGroupForm.get('visibility').value
        this.groupManagementService.updateGroup(name, this.editId, visibility).subscribe((response: any) => {
            if (
                response.statusCode === 200
            ) {
                this.Responce = response
                this.status = response.message
                this.spinner = false;
                this.popup = true;
            }
        },

            (error: HttpErrorResponse) => {
                let err = this.groupManagementService.badError
                this.Responce = err;
                this.status = err.message
                this.popup = true;
                this.spinner = false;

            }
        )

    }

    editContact() {

        this.groupManagementService.editContact(this.newGroupForm).subscribe((response: any) => {
            if (response.statusCode == 200) {
                this.Responce = response
                this.status = response.message
                this.spinner = false;
                this.popup = true;
            }
        },
            (error: HttpErrorResponse) => {
                let err = this.groupManagementService.badError
                this.Responce = err;
                this.status = err.message
                this.popup = true;
                this.spinner = false;

            })


    }


    addContact() {

        this.groupManagementService.addContacts(this.newGroupForm).subscribe((response: any) => {
            if (response.statusCode == 201) {
                this.Responce = response
                this.status = response.message
                this.spinner = false;
                this.popup = true;

            }
        },
            (error: HttpErrorResponse) => {
                let err = this.groupManagementService.badError
                this.Responce = err;
                this.status = err.message;
                this.popup = true;
                this.spinner = false;

            })
    }

}
