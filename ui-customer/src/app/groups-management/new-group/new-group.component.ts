import { HttpErrorResponse } from "@angular/common/http";
import { Component, OnDestroy, ViewChild } from "@angular/core";
import {
    FormBuilder,
    FormControl,
    FormGroup,
    Validators
} from "@angular/forms";
import { Router } from "@angular/router";
import { LocalStorageService } from "src/app/authentication/local-storage.service";
import { maxLengthValidator } from "src/app/campaigns/validators/maxlength-validator";
import { minLengthValidator } from "src/app/campaigns/validators/minlength-validator";
import { CONSTANTS } from "src/app/shared/campaigns.constants";
import { CommonService } from "src/app/shared/commonservice";
import { FileUploaderComponent } from "src/app/shared/file-uploader/file-uploader.component";
import { GroupsManagementService } from "../groups-management.service";

@Component({
    selector: "app-new-group",
    templateUrl: "./new-group.component.html",
    styleUrls: ["./new-group.component.css"]
})
export class NewGroupComponent implements OnDestroy {
    apiError: boolean;
    constructor(
        private fb: FormBuilder,
        private router: Router,
        private localStorage: LocalStorageService,
        private groupManagementService: GroupsManagementService,
        private commonService:CommonService

    ) {
        const user = this.localStorage.getLocal("user");
        this.userDetail = JSON.parse(user)

        
    }
    ngOnDestroy(): void {
        if (this.loader) {
            this.loader.unsubscribe();
        }
        
    }
    
    
    userDetail: any
    
    //access token Detail
    accessTokendetail=this.commonService.tokenDecoder();

    fileContentOrder = CONSTANTS.OTM_FILE_CONTENT;

    createModal = false;

    popup = false;

    selectedDelimiter = "N/A";

    uniqueNameError = CONSTANTS.ERROR_DISPLAY.uniqeGroupName
    groupNameInfoTxt = CONSTANTS.INFO_TXT.campaignName;
    blue =
        "bg-blue-500 relative flex items-center justify-center text-white rounded border border-gray-300 px-3 py-2 mt-3 mr-3 cursor-pointer sm:flex sm:justify-between focus-within:ring-1 focus-within:ring-offset-2";

    grey =
        "bg-gray-100 text-gray-600 border-dashed relative flex items-center justify-center rounded border border-gray-300 px-3 py-2 mt-3 mr-3 cursor-pointer sm:flex sm:justify-between focus-within:ring-1 focus-within:ring-offset-2";

    cancelMessage = CONSTANTS.INFO_TXT.groupEditCancelMessage;

    public loading$ = this.groupManagementService.loadingGroupNames$.asObservable();

    public minimumLength = CONSTANTS.minimumGroupNameLength;

    public minLengthError = CONSTANTS.ERROR_DISPLAY.fieldMinLength;

    public maximumLength = CONSTANTS.maximumGroupNameLength;

    public maxLengthError = CONSTANTS.ERROR_DISPLAY.fieldMaxLength;

    public loader = this.groupManagementService.creationloading.subscribe((data) => this.spinner = data)

    public spinner = false;

    pattern_validation = CONSTANTS.pattern_validation;

    public splCharsError = CONSTANTS.ERROR_DISPLAY.campaignNameSplChars;

    public allowedSplChars = CONSTANTS.allowed_special_characters;



    @ViewChild(FileUploaderComponent, { static: false })
    dropzoneControl: FileUploaderComponent;

    ngAfterViewChecked(): void {
        this.groupManagementService.setDropzoneControl(this.dropzoneControl);
        localStorage.removeItem("fromPage")
        localStorage.setItem("fromPage", "group")

    }
    newGroupForm = this.fb.group({
        name: [
            "",
            Validators.compose([
                Validators.required,
                minLengthValidator(this.minimumLength),
                maxLengthValidator(this.maximumLength),
                Validators.pattern(this.pattern_validation)

            ])
        ],
        visibility: ["private"],
        groupType: ["normal"],
        files: ["", Validators.required],
        removeDuplicates: [""]

    });


    detailsFromUploader: any[] = [];

    processedFiles: any[] = []

    getFileUploadSectionData(event: any) {

        this.detailsFromUploader = event.files
        this.processedFiles = []
        event.files.forEach((element: any) => {
            // let  ele=element.successFiles;
            let files =
            {

                "filename": element.originalname,
                "r_filename": element.r_filename,
                "count": element.total,

            }
            this.processedFiles.push(files)
        });

        this.newGroupForm.controls.files.setValue(event.files);

        if (event.files.every((element: null | any) => element === null)) {
            this.newGroupForm.controls.files.setErrors({
                server: { uploadError: true }
            });
        }


    }

    status: string;


    // g_name:string=this.newGroupForm.controls.name.value;
    // g_type:string=this.newGroupForm.controls.groupType.value;
    // g_visiblity:string=this.newGroupForm.controls.visibility.value;

    apiRes: string
    message: string
    Responce: { message: string, statusCode: number }
    responseCode: number
    // onSubmit() {
    //     let name: string = this.newGroupForm.controls.name.value;
    //     let type: string = this.newGroupForm.controls.groupType.value;
    //     let visiblity: string = this.newGroupForm.controls.visibility.value;

    //     this.groupManagementService.postgroup(name, type, visiblity, this.processedFiles).subscribe((responcedata) => {
    //         this.Responce = responcedata
    //         this.responseCode = responcedata.statusCode
    //         this.apiRes = (responcedata.statusCode > 199 && responcedata.statusCode < 299) ? "success" : "failure"
    //         this.status = responcedata.message
    //         this.message = responcedata.message
    //         this.popup = true;

    //     })

    // }


    onCreate() {
        this.validation();
        //  this.dropzoneControl.validateDropzoneArea();
        if (this.newGroupForm.valid) {
            // this.onSubmit();
        }

    }

    tryAgain(event: any) {
        //  this.onSubmit();
    }

    modalClose(event: boolean) {
        this.popup = false;
    }

    modalcontinue(event: boolean) {

        this.router.navigate(["/groups"]);
    }
    get name() {
        return this.newGroupForm.controls.name
    }

    validation() {
        Object.keys(this.newGroupForm.controls).forEach((field) => {
            const control = this.newGroupForm.get(field);
            if (control instanceof FormControl) {
                control.markAsTouched({ onlySelf: true });
            }
        });
        this.cleanForm(this.newGroupForm)
    }

    checkUniqeNameExist(event: any) {
        this.apiError = false;
        let Gname = (event.target.value as string).trim();
        this.name.setValue(Gname);
        if (Gname.length > 0 || !this.name.errors) {
            Gname = Gname.toLowerCase();
            this.groupManagementService.uniqueNameValidation(Gname)
                .subscribe((data) => {

                    if (!data.isUnique) {
                        this.newGroupForm.controls.name.setErrors({ groupNameExist: true })
                    }

                },
                    (error: HttpErrorResponse) => {
                        this.newGroupForm.controls.name.setErrors({
                            apiRequestError: true
                        });
                        // this.apiError = true;
                    }
                );
        }

    }


    cleanForm(formGroup: FormGroup) {
        Object.keys(formGroup.controls).forEach((key) => {
            const control = formGroup.get(key);
            if (control instanceof FormControl) {
                var ele = control.value
                if (ele instanceof String)
                    control.setValue(control.value.trim())
            }

        }


        );
    }

    retry() {
        const focus = document.getElementById("name") as HTMLImageElement;
        focus.focus();
        focus.blur();
    }
    focus(event) {
        if (event = true) {
            const focus = document.getElementById("name") as HTMLImageElement;
            focus.focus();
        }

        // focus.scrollIntoView();
    }


    backEnter(event: any) {
        if (event.keyCode == 13) {
            this.router.navigate(['/groups'])
        }
    }
}
// export class uploadedFile {
//     count: string;
//     count_human: string;
//     filename: string;
//     r_filename: string;
// }