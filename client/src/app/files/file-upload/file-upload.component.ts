import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { ServerService } from '../../common/server.service';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.component.html',
  styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit {
  @ViewChild('fileInput') inputEl: ElementRef;

  constructor(public _server: ServerService) { }

  ngOnInit() {
  }

  public uploadFiles(event:any){
    const inputEl: HTMLInputElement = this.inputEl.nativeElement;
    const fileCount: number = inputEl.files.length;
    if (fileCount > 0) { // a file was selected
        for (let i = 0; i < fileCount; i++) {
            const formData = new FormData();
            const file  = inputEl.files.item(i);
            formData.append('file', file);
            formData.append('filename', 'test2');
            this._server.uploadDocument(formData);
        }
    }
  }

}
