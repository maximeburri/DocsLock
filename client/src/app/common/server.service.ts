import { Injectable } from '@angular/core';
import { SailsService } from 'angular2-sails';
import { Observable, Subject } from 'rxjs';
import { HttpClient } from '@angular/common/http';

const API_URL = 'http://127.0.0.1:1337';

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
      return this.sailsService.post(`/group/${group.id}/devices/${device.id}`).toPromise()
        .then(reponseFunction)
        .catch(error => console.log(error));
    // No group, remove
    else if (device.group.id)
      return this.sailsService.delete(`/group/${device.group.id}/devices/${device.id}`).toPromise()
        .then(reponseFunction)
        .catch(error => console.log(error));
  }

  public post(url: any, data?: any): Observable<any> {
    return this.sailsService.post(url, data);
  }

  public get(url: any, data?: any): Observable<any> {
    return this.sailsService.get(url, data);
  }

  public put(url: any, data?: any): Observable<any> {
    return this.sailsService.put(url, data);
  }

  public delete(url: any, data?: any): Observable<any> {
    return this.sailsService.delete(url, data);
  }

  public setGroupLocked(group: any, isLocked: boolean) {
    return this.put(`/group/${group.id}/`, { isLocked: isLocked }).toPromise()
      .then((result) => {
        this.groups.forEach((g) => {
          if (g.id === group.id) {
            g.isLocked = result.data.isLocked;
          }
        }
        );
        this.pushDevices(group);
      })
      .catch(error => console.error(error));
  }

  public pushDevice(device: any) {
    return this.post(`/device/${device.id}/push`).toPromise()
    .then((result) => {})
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
    .then((result) => {
      // Copy without change reference (to change everything)
      Object.assign(group, result.data);
    })
    .catch(error => console.error(error));
  }

  public uploadDocument(document: any) {
    // We need to post over HTTP instead of this.http
    return this.http.post(`${API_URL}/document/`,
      document
    ).toPromise()
    .then((result) => {
      // Update documents array
      if(result && result[0]) {
        this.documents.push(result[0]);
      }
    })
    .catch(error => console.error(error));
  }

  public downloadDocument(document : any){
    window.location.href = 'http://127.0.0.1:1337/document/download/' + document.id;
  }

  public removeDocument(document : any){
    return this.http.delete(`${API_URL}/document/${document.id}`).toPromise()
    .then((result) => {
      // Update documents array
      if(result) {
        const index = this.documents.findIndex((d) => d.id === document.id);
        this.documents.splice(index, 1);
      }
    })
    .catch(error => console.error(error));
  }
}
