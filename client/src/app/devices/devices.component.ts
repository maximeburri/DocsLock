import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'devices-cmp',
    templateUrl: './devices.component.html',
    styleUrls: ['./devices.component.css']
})

export class DevicesComponent implements OnInit{
    public devices = [];

    ngOnInit(){
        this.devices = [
            {
                "id" : 1,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
            },
            {
                "id" : 2,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
            },
            {
                "id" : 3,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
            },
            {
                "id" : 4,
                "os" : "android",
                "type" : "tablet",
                "mac" : "AA::BB::CC::DD::EE"
            }
        ];
    }
}
