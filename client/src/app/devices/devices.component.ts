import { Component, OnInit } from '@angular/core';
import {
    trigger,
    state,
    style,
    animate,
    transition
} from '@angular/animations';
import { ServerService } from '../common/server.service';

import { MatDialog } from '@angular/material';
import { DocumentsDialogComponent } from './documents-dialog/documents-dialog.component';
import { EditGroupDialogComponent } from './edit-group-dialog/edit-group-dialog.component';
import { ConfirmationDialogComponent } from '../common/confirmation-dialog/confirmation-dialog.component';

@Component({
    selector: 'devices-cmp',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.css'],
    animations: [
        trigger('rowState', [
            state('new',
                style({})
            ),
            transition('void => new',
                animate('100ms ease-in', style({
                    backgroundColor: '#eee',
                    transform: 'scale(1.3)'
                }))
            )
        ])
    ]
})

export class DevicesComponent implements OnInit {
    public devices = [];
    public groups = [];
    constructor(private _server : ServerService, public dialog: MatDialog) {
        this._server = _server;
    }

    ngOnInit() {

        /* device : 
        {
                "id" : 1,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
        }
        */
        this._server.getDevices().then(devices => this.devices = devices);
        this._server.getGroups().then(groups => this.groups = groups);
    }

    setGroupLocked(group: any, isLocked: boolean) {
        this._server.setGroupLocked(group, isLocked);
    }

    openDocumentsDialog(group: any): void {
        this.dialog.open(DocumentsDialogComponent, {
            data: { group: group }
        });
    }

    public openNewGroupDialog() {
        const devicesToGroup = this.devices.filter(d => d.isSelected);
        const dialogRef = this.dialog.open(EditGroupDialogComponent, {
            data : {
                mode: 'new'
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            console.log("Group created");
        });
      }

    public removeGroup(group) {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
            data: {
                title : 'Confirmation',
                type : 'remove',
                name : group.name,
                irrevocable : true,
                entityName : 'the group'
            }
          });
        dialogRef.afterClosed().subscribe(confirmed => {
            console.log(confirmed);
            if(confirmed)
                this._server.removeGroup(group);
        });
    }

    public editGroup(group) {
        const devicesToGroup = this.devices.filter(d => d.isSelected);
        const dialogRef = this.dialog.open(EditGroupDialogComponent,{
            data : {
                mode: 'edit',
                group : group
            }
        });
        dialogRef.componentInstance.name = group.name;
        dialogRef.afterClosed().subscribe(result => {
            console.log("Group created");
        });
    }
}
