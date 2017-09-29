import { Component, OnInit, Input, Pipe, PipeTransform  } from '@angular/core';
import {
  trigger,
  state,
  style,
  animate,
  transition
} from '@angular/animations';

@Component({
  selector: 'table-devices',
  templateUrl: './table-devices.component.html',
  styleUrls: ['./table-devices.component.scss'],
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
export class TableDevicesComponent implements OnInit {

  @Input() devices: any;
  @Input() group?: number;

  constructor() { }

  ngOnInit() {
  }

}
