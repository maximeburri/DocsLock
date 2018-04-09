import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';
import { RouterModule } from '@angular/router';

import { AppRoutingModule } from './app.routing';
import { ComponentsModule } from './components/components.module';

import { AppComponent } from './app.component';

import { UserProfileComponent } from './user-profile/user-profile.component';
import { DevicesComponent, RemoveGroupConfirmationMessage } from './devices/devices.component';
import { FilesComponent } from './files/files.component';
import { SailsModule } from 'angular2-sails';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { CdkTableModule } from '@angular/cdk/table';

import {
  MatAutocompleteModule, MatButtonModule, MatButtonToggleModule, MatPaginatorModule,
  MatCardModule, MatCheckboxModule, MatChipsModule, MatDatepickerModule,
  MatDialogModule, MatGridListModule, MatIconModule, MatInputModule,
  MatListModule, MatMenuModule, MatProgressBarModule, MatProgressSpinnerModule,
  MatRadioModule, MatSelectModule, MatSidenavModule, MatSliderModule, MatSortModule,
  MatSlideToggleModule, MatSnackBarModule, MatTableModule, MatTabsModule, MatToolbarModule,
  MatTooltipModule, MatFormFieldModule, MatExpansionModule, MatStepperModule
} from '@angular/material';
import { TableDevicesComponent } from './devices/table-devices/table-devices.component';
import { DeviceGroupPipe } from './devices/devices-group/devices-group.pipe';
import { DevicesActionsComponent } from './devices/devices-actions/devices-actions.component';
import { DevicesSelectedPipe } from './devices/devices-selected/devices-selected.pipe';
import { DocumentsDialogComponent } from './devices/documents-dialog/documents-dialog.component';

import { ServerService } from './common/server.service';
import { FileUploadComponent } from './files/file-upload/file-upload.component';

import { HttpClientModule } from '@angular/common/http';
import { NewGroupDialogComponent } from './devices/new-group-dialog/new-group-dialog.component';

import { ConfirmationDialogComponent } from './common/confirmation-dialog/confirmation-dialog.component';

@NgModule({
  declarations: [
    AppComponent,
    DevicesComponent,
    RemoveGroupConfirmationMessage,
    FilesComponent,
    UserProfileComponent,
    TableDevicesComponent,
    DeviceGroupPipe,
    DevicesActionsComponent,
    DevicesSelectedPipe,
    DocumentsDialogComponent,
    FileUploadComponent,
    NewGroupDialogComponent,
    ConfirmationDialogComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    ComponentsModule,
    RouterModule,
    AppRoutingModule,
    SailsModule.forRoot(),
    BrowserAnimationsModule,
    HttpClientModule,
    
    CdkTableModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDatepickerModule,
    MatDialogModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSelectModule,
    MatSlideToggleModule,
    MatSliderModule,
    MatSidenavModule,
    MatSnackBarModule,
    MatStepperModule,
    MatTabsModule,
    MatToolbarModule,
    MatTooltipModule,
    MatPaginatorModule,
    MatSortModule,
    MatTableModule,

    MatIconModule, MatMenuModule
  ],
  entryComponents: [
    DocumentsDialogComponent,
    NewGroupDialogComponent,
    ConfirmationDialogComponent
  ],
  providers: [
    ServerService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
