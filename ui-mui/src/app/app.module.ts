import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { CoreModule } from './core/core.module';
import { LoginLayoutComponent } from './layouts/login-layout/login-layout.component';
import { AuthenticationModule } from './authentication/authentication.module';
import { FrameComponent } from './layouts/frame/frame.component';

@NgModule({
  declarations: [
    AppComponent,
    FrameComponent,
    LoginLayoutComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,

    CoreModule,
    CommonModule,
   
    AuthenticationModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
