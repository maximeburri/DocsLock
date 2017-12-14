import { Component, OnInit } from '@angular/core';

import { ServerService } from '../common/server.service';
import { generate } from 'rxjs/observable/generate';

@Component({
    selector: 'files-cmp',
    templateUrl: './files.component.html',
    styleUrls: ['./files.component.css']
})

export class FilesComponent implements OnInit {
    public documents = [];
    constructor(private _server: ServerService) {
        this._server = _server;
    }

    ngOnInit() {
        this._server.getDocuments().then(documents => this.documents = documents);
    }

    public download(document: any) {
        this._server.downloadDocument(document);
    }

    public remove(document: any) {
        this._server.removeDocument(document).then(d => {
            console.log(d);
            if (document.groups) {
                document.groups.forEach(g => {
                    this._server.pushDevices(g);
                });
            }
        });
    }
}

