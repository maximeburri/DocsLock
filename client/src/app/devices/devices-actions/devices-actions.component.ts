import { Component, OnInit, Input } from '@angular/core';
import { DevicesSelectedPipe } from '../devices-selected/devices-selected.pipe';
import { ServerService } from '../../common/server.service';

@Component({
  selector: 'devices-actions',
  templateUrl: './devices-actions.component.html',
  styleUrls: ['./devices-actions.component.scss']
})
export class DevicesActionsComponent implements OnInit {
  @Input() devices: any[] = [];
  @Input() groups: any[] = [];
  @Input() currentGroup: any;

  constructor(private server : ServerService) {
  }

  ngOnInit() {
  }

  public groupTo(group : any) {
    this.devices.forEach(device => {
      if(device.isSelected)
        this.server.setDeviceGroup(device, group);
    });
  }

  public selectAll() {
    this.devices.forEach(device => {
      device.isSelected = true;
      console.log(device);
    });
  }

  public selectNone() {
    this.devices.forEach(device => {
      device.isSelected = false;
    });
  }
}