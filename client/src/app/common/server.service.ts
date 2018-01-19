import { Injectable } from '@angular/core';
import { SailsService } from 'angular2-sails';
import { Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'http://' + document.location.hostname + ':1337';

@Injectable()
export class ServerService {
  private devices = [];
  private groups = [];
  private documents = [];

  private promiseDevices: Promise<any>;
  private promiseGroups: Promise<any>;
  private promiseDocuments: Promise<any>;

  constructor(private sailsService: SailsService, public http: HttpClient) {
    this.sailsService
      .connect(API_URL)
      .subscribe(
      t => console.log("Connected"),
      e => console.error(e)
      );
  }

  public getDevices(): Promise<any> {
    if (!this.promiseDevices)
      this.promiseDevices = this.getArrayDataModelAndUpdate("device", this.devices);
    return this.promiseDevices;
  }

  public getGroups(): Promise<any> {
    if (!this.promiseGroups)
      this.promiseGroups = this.getArrayDataModelAndUpdate("group", this.groups);
    return this.promiseGroups;
  }

  public getDocuments(): Promise<any> {
    if (!this.promiseDocuments)
      this.promiseDocuments = this.getArrayDataModelAndUpdate('document', this.documents);
    return this.promiseDocuments;
  }


  private getArrayDataModelAndUpdate(model: string, arrayResult: Array<any>): Promise<any> {
    this.sailsService.on(model).subscribe(
      change => {
        console.log(change)
        if (change.verb) {
          // Switch for device change action
          switch (change.verb) {
            case "created":
              change.data.rowState = "new";
              arrayResult.push(change.data);
              break;
            case "updated":
              change.data.rowState = "new";

              // Warning 
              // With api rest : group.id
              // With websocket update event : group is the id 
              // Copy with 'Object.assign' the entire group
              // TODO : if an other user set group in same time...
              //if (deviceChange.data.group)
              //  deviceChange.data.group = Object.assign({},
              //    this.getGroupById(deviceChange.data.group));

              // Copy the change
              Object.assign(
                arrayResult.find((d) => d.id == change.id),
                change.data
              );
              break;
            case "destroyed":
              let index = arrayResult.findIndex((d) => d.id == change.id);
              arrayResult.splice(index, 1);
              break;
          }
        }
      },
      error => console.error(error)
    );
    return this.sailsService.get(`/${model}?limit=0`).toPromise().then(
      result => {
        Object.assign(arrayResult, result.data);
        return arrayResult;
      }).catch(
      error => console.error(error)
      );
  }

  public setDeviceGroup(device, group?) {
    let reponseFunction = response => {
      // Update the group of device
      device.group = group;
      device.isSelected = false;
      device.rowState = "new-" + (group == null) ? null : group.id;
      this.pushDevice(device);
    };
    // Add to the goud group
    if (group)
      return this.post(`/group/${group.id}/devices/${device.id}`).toPromise()
        .then(reponseFunction)
        .catch(error => console.log(error));
    // No group, remove
    else if (device.group.id)
      return this.delete(`/group/${device.group.id}/devices/${device.id}`).toPromise()
        .then(reponseFunction)
        .catch(error => console.log(error));
  }

  public post(url: any, data?: any, updateAllData?: boolean, useHttpClient?: boolean): Observable<any> {
    if(useHttpClient)
      return this.http.post(API_URL + url, data);
    else {
      const observable = this.sailsService.post(url, data);
      if(updateAllData === undefined || updateAllData)
        observable.subscribe(result => { this.updateAllData();});
      return observable;
    }
  }

  public get(url: any, data?: any, updateAllData?: boolean, useHttpClient?: boolean): Observable<any> {
    if(useHttpClient)
      return this.http.get(API_URL + url, data);
    else {
      const observable = this.sailsService.get(url, data);
      if(updateAllData === undefined || updateAllData)
        observable.subscribe(result => {this.updateAllData()});
      return observable;
    }
  }

  public put(url: any, data?: any, updateAllData?: boolean, useHttpClient?: boolean): Observable<any> {
    if(useHttpClient)
      return this.http.put(API_URL + url, data);
    else {
      const observable = this.sailsService.put(url, data);
      if(updateAllData === undefined || updateAllData)
        observable.subscribe(result => {this.updateAllData()});
      return observable;
    }
  }

  public delete(url: any, data?: any, updateAllData?: boolean, useHttpClient?: boolean): Observable<any> {
    if(useHttpClient)
      return this.http.delete(API_URL + url, data);
    else {
      const observable = this.sailsService.delete(url, data);
      if(updateAllData === undefined || updateAllData)
        observable.subscribe(result => {this.updateAllData()});
      return observable;
    }
  }

  public updateAllData(): any {
    this.updateModel('device', this.devices);
    this.updateModel('group', this.groups);
    this.updateModel('document', this.documents);
  }

  private updateModel(modelName: String, modelVariable: any): any {
    const observable = this.sailsService.get(`/${modelName}?limit=0`);
    observable.subscribe(
      result => {
        // 1. reset the array while keeping its reference
        modelVariable.length = 0;
        // 2. fill the first array with items from the second
        [].push.apply(modelVariable, result.data);
        console.log(modelName + " updated", result.data);
      },
      error => console.error(error)
    );
  }

  public setGroupLocked(group: any, isLocked: boolean) {
    return this.put(`/group/${group.id}/`, { isLocked: isLocked }).toPromise()
      .then((result) => {
        this.pushDevices(group);
      })
      .catch(error => console.error(error));
  }

  public pushDevice(device: any) {
    return this.post(`/device/${device.id}/push`, {}, false /*donst update data*/).toPromise()
    .catch(error => console.error(error));
  }

  public pushDevices(group : any){
    this.devices.forEach((device) => {
      if(device.group && device.group.id === group.id){
        this.pushDevice(device);
      }
    });
  }

  public setGroupDocumentsId(group: any, documentsId: any[]) {
    return this.post(`/group/${group.id}/`, {documents: documentsId}).toPromise()
    .catch(error => console.error(error));
  }

  public uploadDocument(document: any) {
    // We need to post over HTTP instead of this.http
    return this.post(`/document/`,
      document, false, true /* use http client angular */
    ).toPromise()
    .then(result => this.updateAllData())
    .catch(error => console.error(error));
  }

  public downloadDocument(document : any){
    window.location.href = 'http://127.0.0.1:1337/document/download/' + document.id;
  }

  public removeDocument(document : any){
    return this.delete(`/document/${document.id}`).toPromise()
    .then(result => {
      // The actualization remove bad, don't know why...
    })
    .catch(error => console.error(error));
  }

  public newGroup(name: string) {
    return this.post(`/group/`, {name: name}).toPromise()
    .catch(error => console.error(error));
  }
}
