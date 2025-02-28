import { Component, NgModule } from "@angular/core";
import { Router, RouterModule,Routes } from "@angular/router";
import { DltfileUploadComponent } from "src/app/dltfile-upload/dltfile-upload.component";
import { UploadfileComponent } from "src/app/dltfile-upload/uploadfile/uploadfile.component";
import { DltoverviewComponent } from "src/app/dltfile-upload/dltoverview/dltoverview.component";
import { DltuploadlistComponent } from "src/app/dltfile-upload/dltuploadlist/dltuploadlist.component";
const routes : Routes  = [
    // {path:"dltupload" , component:UploadfileComponent},
    // {path:"dltoverview" , component:DltoverviewComponent}
    {path:"" , component:DltfileUploadComponent,
    children: [
        { path: "fileupload", component: UploadfileComponent },

        {path:"overview" , component:DltoverviewComponent,},
        
        {path:"uploadlist", component:DltuploadlistComponent}
     
    ]
    }
]

@NgModule({
    imports : [RouterModule.forChild(routes)],
    exports : [RouterModule]
    
})

export class dltFIleUploadRoutingModule {}