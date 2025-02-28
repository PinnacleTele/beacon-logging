import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './authentication/login/login.component';
import { FrameComponent } from './layouts/frame/frame.component';
import { LoginLayoutComponent } from './layouts/login-layout/login-layout.component';


const routes: Routes = [
  {
    path: 'main',
    component: FrameComponent,
    
  },
  {
    path: '',
    component: LoginLayoutComponent,
    
    children: [
      {
        path: 'login',
        component: LoginComponent
      },
     
    ]
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
