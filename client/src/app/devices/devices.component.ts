import { Component, OnInit } from '@angular/core';
import { SailsService } from "angular2-sails";
declare var io;

@Component({
    selector: 'devices-cmp',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.css']
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

        io.socket.get('/device', function(data, jwr) {
            io.socket.on('new_entry', function(entry) {
            });
        });


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
