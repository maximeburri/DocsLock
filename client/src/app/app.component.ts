import { Component, OnInit } from '@angular/core';
import { Location, LocationStrategy, PathLocationStrategy } from '@angular/common';
import { ServerService } from './common/server.service';
import { Router } from '@angular/router';

declare const $: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  providers: [ServerService]
})
export class AppComponent implements OnInit {

  constructor(public location: Location, public router:Router) { }

  ngOnInit() {

  }

  goPage(pageName){
    this.router.navigate([pageName])
  }
}
