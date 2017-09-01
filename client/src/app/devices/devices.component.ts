import { Component, OnInit } from '@angular/core';
import { SailsService } from "angular2-sails";
import {
    trigger,
    state,
    style,
    animate,
    transition
} from '@angular/animations';

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

    constructor(private _sailsService: SailsService) { }

    ngOnInit() {
        this._sailsService
            .connect("http://127.0.0.1:1337")
            .subscribe(
            t => console.log("Connected"),
            e => console.error(e)
            );

        /* device : 
        {
                "id" : 1,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
        }
        */
        this._sailsService.get("/device?limit=0").subscribe(
            devices => this.devices = devices.data,
            error => console.error(error)
        );

        this._sailsService.on("device").subscribe(
            deviceChange => {
                console.log(deviceChange)
                if (deviceChange.verb) {
                    // Switch for device change action
                    switch (deviceChange.verb) {
                        case "created":
                            deviceChange.data.rowState = "new";
                            this.devices.push(deviceChange.data);
                            break;
                        case "updated":
                            deviceChange.data.rowState = "new";
                            Object.assign(
                                this.getDeviceById(deviceChange.id),
                                deviceChange.data
                            );
                            break;
                        case "destroyed":
                            this.devices = this.devices.filter(
                                (device) => device.id != deviceChange.id
                            );
                            break;
                    }
                }

            },
            error => console.error(error)
        );
    }

    private getDeviceById(id: number) {
        return this.devices.find(
            (device) => device.id == id
        )
    }
}
