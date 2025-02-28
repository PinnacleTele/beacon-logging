import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ERROR } from '../campaigns/error.data';
import { CONSTANTS_URL } from '../shared/compaign.url';
import { FileUploaderComponent } from '../shared/file-uploader/file-uploader.component';
import { GroupModel } from './groupsMangement.group.model';

@Injectable({
    providedIn: 'root'
})
export class GroupsManagementService {

    constructor(private http: HttpClient) { }

    BASE_URL = CONSTANTS_URL.GLOBAL_URL;

    ALL_GROUPS_URL = CONSTANTS_URL.ALL_GROUPS_URL;

    DELETE_URL = CONSTANTS_URL.DELETE_GROUP_URL;

    DELETE_API_URL = this.BASE_URL + this.DELETE_URL;

    NEW_GROUP_ENDPOINT = CONSTANTS_URL.NEW_GROUP_URL;

    NEW_GROUP_POST_URL = this.BASE_URL + this.NEW_GROUP_ENDPOINT;

    UPDATE_URL = CONSTANTS_URL.UPDATE_GROUP_URL;

    GROUP_INFO_URL = CONSTANTS_URL.GROUP_INFO_URL;

    UNIQE_NAME_API_URL = CONSTANTS_URL.UNIQE_NAME_API_URL;

    CONTACT_LIST_API = CONSTANTS_URL.CONTACT_LIST_API;

    EDIT_CONTACT_URL = CONSTANTS_URL.EDIT_CONTACT_URL;

    ADD_CONTACTS_URL = CONSTANTS_URL.ADD_CONTACTS_URL;

    DELETE_CONTACT_API = CONSTANTS_URL.DELETE_CONTACT_API;

    routedGroup: any;

    editingGroup: any;

    groups: GroupModel[] = [];

    //GroupList API call
    groupLoading = new BehaviorSubject<boolean>(false)

    public creationloading = new BehaviorSubject<boolean>(false);

    findAllGroups() {
        this.groupLoading.next(true)
        return this.http.
            get<GroupModel[]>(this.BASE_URL + this.ALL_GROUPS_URL)
            .pipe(
                map((responseData) => {
                    this.groups = [];
                    for (const key in responseData) {
                        if (
                            Object.prototype.hasOwnProperty.call(
                                responseData,
                                key
                            )
                        ) {
                            this.groups.push({
                                ...responseData[key]
                            });
                        }
                    }
                    this.groupLoading.next(false)
                    return responseData
                }),
                catchError((err) => {
                    this.groupLoading.next(false)
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })


            )


    }

    badError: any;

    //newGroup create API call

    postgroup(name: string, type: string, visiblity: string, fileDeatils: any[]) {
        let group = {
            "g_name": name,
            "g_type": type,
            "g_visibility": visiblity,
            "files": fileDeatils
        }
        this.creationloading.next(true);

        return this.http.post<{ message: string, statusCode: number }>(this.NEW_GROUP_POST_URL, group)
            .pipe
            (map((responceData) => {
                this.creationloading.next(false);

                return responceData

            }),
                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })

            )
    }

    //delete Group API call
    deleteGroup(ids: string[]) {

        let groupIds = { "ids": ids }
        return this.http.post<{ statusCode: number, message: string }>(this.DELETE_API_URL, groupIds)
            .pipe(
                map((responceData) => {
                    return responceData
                }),

                catchError((err) => {

                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                    // return throwError(err)
                })

            )

    }

    httpOptions = {
        headers: new HttpHeaders({
            Accept: "application/json",
            "Content-Type": "application/json"
        })
    };





    public updateLoading = new BehaviorSubject<boolean>(false);

    getGroupInfo(groupId: string) {
        this.searchLoading.next(true)
        return this.http.get(this.BASE_URL + this.GROUP_INFO_URL + groupId).
            pipe(map((responseData) => {
                this.searchLoading.next(false)
                return responseData
            }),

                catchError((err) => {
                    this.searchLoading.next(false)
                    return throwError(err)
                })

            )

    }



    updateGroup(name: string, id: string, visibility: string) {
        let file = { "g_name": name, "id": id, "g_visibility": visibility }
        this.updateLoading.next(true)
        return this.http.post(this.BASE_URL + this.UPDATE_URL, file)
            .pipe(map((res) => {
                this.updateLoading.next(false)
                return res

            }),
                catchError((err) => {
                    if (err.status == 0) {

                        let setError = ERROR.REQUEST_NOT_SEND;

                        this.badError = setError;
                    } else {
                        let setError = ERROR.SOMETHING_WENT_WRONG;
                        this.badError = setError;
                    }
                    return this.badError;
                })



            )
    }



    public loadingGroupNames$ = new BehaviorSubject<boolean>(false);

    isUNIQE_URL = this.BASE_URL + this.UNIQE_NAME_API_URL

    uniqueNameValidation(Groupname: string) {
        this.loadingGroupNames$.next(true);
        return this.http.get<{ isUnique: string }>(this.isUNIQE_URL + Groupname).pipe(
            map((responseData) => {
                this.loadingGroupNames$.next(false);
                let value = responseData

                return value;
            }),
            catchError((err) => {
                this.loadingGroupNames$.next(false);
                return throwError(err);
            })
        );
    }

    searchLoading = new BehaviorSubject<boolean>(false);
    findAllContactList(group_Id: string, group_type: string, match?: string) {
        if (!match) {
            match = "*"
        }
        this.searchLoading.next(true)
        return this.http.get(this.BASE_URL + this.CONTACT_LIST_API + group_Id + "&g_type=" + group_type + "&match=" + match).pipe(
            map((responseData) => {
                this.searchLoading.next(false)
                return responseData
            }),
            catchError((err) => {
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }
                this.searchLoading.next(false)
                return this.badError;
            })
        )

    }


    deleteContact(groupId: string, groupType: string, mobiles: string[]) {
        let file = {
            "g_id": groupId,
            "g_type": groupType,
            "mobiles": mobiles

        }

        return this.http.post(this.BASE_URL + this.DELETE_CONTACT_API, file).pipe(
            map((responseData) => {

                return responseData

            }),
            catchError((err) => {
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }
                return this.badError;
            })
        )
    }




    addContacts(addContactForm: any) {
        let name = addContactForm.controls.name.value
        let email = addContactForm.controls.email.value
        let mobile = addContactForm.controls.mobile.value
        let g_id = addContactForm.controls.g_id.value
        let g_type = addContactForm.controls.g_type.value
        let files: any = []
        if (addContactForm.controls.files.value) {
            addContactForm.controls.files.value.forEach((element: any) => {
                let file = {
                    "filename": element.originalname,
                    "r_filename": element.r_filename,
                    "count": element.total
                }
                files.push(file)
            });
        }
        let payLoad = { "name": name, "email": email, "mobile": mobile, "g_id": g_id, "g_type": g_type, "files": files }

        return this.http.post(this.BASE_URL + this.ADD_CONTACTS_URL, payLoad).pipe(
            map((responseData) => {
                return responseData

            }),
            catchError((err) => {

                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }
                return this.badError;
            })
        )

    }


    editContact(editContactForm: any) {

        let email
        if (editContactForm.controls.email?.value) {
            email = editContactForm.controls.email?.value
        }
        else {
            email = ""
        }
        let file = {
            "name": editContactForm.controls.name?.value,
            "email": email,
            "mobile": editContactForm.controls.mobile.value,
            "g_id": editContactForm.controls.g_id.value,
            "g_type": editContactForm.controls.g_type.value
        }


        return this.http.post(this.BASE_URL + this.EDIT_CONTACT_URL, file).pipe(
            map((res) => {
                return res
            }),
            catchError((err) => {
                if (err.status == 0) {

                    let setError = ERROR.REQUEST_NOT_SEND;

                    this.badError = setError;
                } else {
                    let setError = ERROR.SOMETHING_WENT_WRONG;
                    this.badError = setError;
                }
                return this.badError;
            })
        )





    }
    dropzoneControl: FileUploaderComponent;

    setDropzoneControl(control: FileUploaderComponent) {
        this.dropzoneControl = control;
    }

    validateAllFormFields(InputForm: FormGroup) {

        const formGroup = InputForm;
        Object.keys(formGroup.controls).forEach((field) => {
            const control = formGroup.get(field);
            if (control instanceof FormControl) {
                if (control.errors) {
                    control.markAsTouched({ onlySelf: true });
                }


            }
        });
        if (this.dropzoneControl) {

            this.dropzoneControl.validateDropzoneArea();
        }

    }

}
