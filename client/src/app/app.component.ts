import { Component, OnInit } from '@angular/core';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { ServerService } from './common/server.service';
import { DocumentsDialogComponent } from './devices/documents-dialog/documents-dialog.component';
import { Router } from '@angular/router';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  constructor(public location: Location, public router:Router) { }

  ngOnInit() {

  }

  goPage(pageName){
    this.router.navigate([pageName])
  }
}
