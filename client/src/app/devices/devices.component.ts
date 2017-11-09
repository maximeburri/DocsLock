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
        const dialogRef = this.dialog.open(DocumentsDialogComponent, {
            data: { group: group }
        });

        dialogRef.afterClosed().subscribe(result => {
            console.log('The dialog was closed');
            console.log(result);
        });
    }
}
