import {Component, Inject} from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import {ServerService} from '../../common/server.service';

@Component({
  selector: 'app-documents-dialog',
  templateUrl: './documents-dialog.component.html',
  styleUrls: ['./documents-dialog.component.scss']
})
export class DocumentsDialogComponent {
  private documents = [];
  private documentsIdIn: number[] = [];
  public documentsInGroup: any[] = [];
  public documentsNotInGroup: any[] = [];

  constructor(
    public dialogRef: MatDialogRef<DocumentsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private server: ServerService
  ) {
    this.documentsIdIn = this.data.group.documents.map(d => d.id);
    server.getDocuments().then(documents => {
      this.documents = documents;
      this.updateDocuments();
    });
  }

  private updateDocuments(){
    this.documentsInGroup = this.getDocumentsInGroup();
    this.documentsNotInGroup = this.getDocumentsNotInGroup();
  }

  private getDocumentsInGroup(): any[] {
    return this.documents.filter(g => g && this.documentsIdIn.includes(g.id));
  }

  private getDocumentsNotInGroup(): any[] {
    return this.documents.filter(g => g && !this.documentsIdIn.includes(g.id));
  }

  public removeDocument(document):void {
    this.documentsIdIn = this.documentsIdIn.filter(d => d !== document.id);
    this.updateDocuments();
  }

  public addDocument(document):void {
    this.documentsIdIn.push(document.id);
    this.updateDocuments();
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

}
