import { NgModule } from '@angular/core';
import { CommonModule, } from '@angular/common';
import { BrowserModule } from '@angular/platform-browser';
import { Routes, RouterModule } from '@angular/router';

import { DevicesComponent } from './devices/devices.component';
import { FilesComponent } from './files/files.component';
import { UserProfileComponent } from './user-profile/user-profile.component';

const routes: Routes = [
  { path: 'devices', component: DevicesComponent },
  { path: 'files', component: FilesComponent },
  { path: 'user-profile', component: UserProfileComponent },
  { path: '', redirectTo: 'devices', pathMatch: 'full' }
];

@NgModule({
  imports: [
    CommonModule,
    BrowserModule,
    RouterModule.forRoot(routes)
  ],
  exports: [
  ],
})
export class AppRoutingModule { }
