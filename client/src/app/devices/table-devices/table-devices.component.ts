import { Component, OnInit, Input, Pipe, PipeTransform  } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations';
import { EditLabelDialogComponent } from './edit-label-dialog/edit-label-dialog.component';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'table-devices',
  templateUrl: './table-devices.component.html',
  styleUrls: ['./table-devices.component.scss'],
  animations: [
    trigger('rowState', [
      state('new',
        style({})
      ),
      transition('void => *',
        animate('100ms ease-in', style({
          backgroundColor: '#eee',
          transform: 'scale(1.3)'
        }))
      )
    ]),
    trigger('tableInOut', [
      state('in', style({transform: 'scale(0)'})),
      transition('void => *', [
        style({transform: 'scale(0.0)'}),
        animate('100ms ease-out')
      ]),
      transition('* => void', [
        animate('100ms ease-out', style({transform: 'scale(1.0)'}))
      ])
    ])
  ]
})
export class TableDevicesComponent implements OnInit {

  @Input() devices: any;
  @Input() group?: number;

  constructor(public dialog: MatDialog) { }

  ngOnInit() {
  }

  public editDeviceLabel(device, label) {
    const dialogRef = this.dialog.open(EditLabelDialogComponent, {
        data: {
          device
        }
      });

    dialogRef.afterClosed().subscribe(result => {
    });
  }

}
