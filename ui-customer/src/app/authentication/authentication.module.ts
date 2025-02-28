import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { AuthenticationRoutingModule } from './authentication-routing.module';
import { LoginComponent } from './login/login.component';
import {  ReactiveFormsModule,FormsModule } from '@angular/forms';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';
import { DotSvgComponent } from './forgot-password/dot-svg/dot-svg.component';
import { SharedModule } from '../shared/shared.module';


@NgModule({
  declarations: [
    LoginComponent,
    ForgotPasswordComponent,
    DotSvgComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    SharedModule,
    AuthenticationRoutingModule
  ],
  exports: [
    LoginComponent
  ]

})
export class AuthenticationModule { }
