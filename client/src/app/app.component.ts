import { Component, OnInit } from '@angular/core';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { ServerService } from './common/server.service';
import { DocumentsDialogComponent } from './devices/documents-dialog/documents-dialog.component';

declare const $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(public location: Location) { }

  ngOnInit() {

  }
}
