import { Component, OnInit } from '@angular/core';
import { SailsService } from "angular2-sails";

@Component({
    selector: 'devices-cmp',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.css']
})

export class DevicesComponent implements OnInit {
    public devices = [];

    constructor(private _sailsService: SailsService) { }

    ngOnInit() {
        this._sailsService.connect("http://localhost:1337");

        /* device : 
        {
                "id" : 1,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
        }
        */
        this._sailsService.get("/device").subscribe(
            devices => this.devices = devices.data,
            error => console.error(error),
            () => console.warn("complete")
        );
    }
}
