import { Component, OnInit, Input } from '@angular/core';
import { DevicesSelectedPipe } from '../devices-selected/devices-selected.pipe';

@Component({
  selector: 'devices-actions',
  templateUrl: './devices-actions.component.html',
  styleUrls: ['./devices-actions.component.scss']
})
export class DevicesActionsComponent implements OnInit {
  @Input() devices: any[] = [];

  constructor() { 
    let f = new DevicesSelectedPipe();
    f.transform(this.devices);
  }

  ngOnInit() {
  }
}
