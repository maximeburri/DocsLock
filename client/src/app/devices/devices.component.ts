import { Component, OnInit } from '@angular/core';
import {
    trigger,
    state,
    style,
    animate,
    transition
} from '@angular/animations';
import { ServerService } from '../common/server.service';

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
    constructor(private _server : ServerService) { 
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

}
