import { Component, OnInit, Input } from '@angular/core';
import { DevicesSelectedPipe } from '../devices-selected/devices-selected.pipe';
import { ServerService } from '../../common/server.service';
import { NewGroupDialogComponent } from '../new-group-dialog/new-group-dialog.component';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'devices-actions',
  templateUrl: './devices-actions.component.html',
  styleUrls: ['./devices-actions.component.scss']
})
export class DevicesActionsComponent implements OnInit {
  @Input() devices: any[] = [];
  @Input() groups: any[] = [];
  @Input() currentGroup: any;

  constructor(private server : ServerService, public dialog: MatDialog) {
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

  public openNewGroupDialog() {
    const devicesToGroup = this.devices.filter(d => d.isSelected);
    const dialogRef = this.dialog.open(NewGroupDialogComponent);
    dialogRef.afterClosed().subscribe(result => {
      const group = result.data;
      if (group) {
        devicesToGroup.forEach( device => {
          this.server.setDeviceGroup(device, group);
        })
      }
    });
  }
}
