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
  private documentsIdInGroup: number[] = [];
  public documentsInGroup: any[] = [];
  public documentsNotInGroup: any[] = [];

  constructor(
    public dialogRef: MatDialogRef<DocumentsDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private server: ServerService
  ) {
    this.documentsIdInGroup = this.data.group.documents.map(d => d.id);
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
    return this.documents.filter(g => g && this.documentsIdInGroup.includes(g.id));
  }

  private getDocumentsNotInGroup(): any[] {
    return this.documents.filter(g => g && !this.documentsIdInGroup.includes(g.id));
  }

  public removeDocument(document): void {
    this.documentsIdInGroup = this.documentsIdInGroup.filter(d => d !== document.id);
    this.updateDocuments();
  }

  public addDocument(document): void {
    this.documentsIdInGroup.push(document.id);
    this.updateDocuments();
  }

  public cancel(): void {
    this.dialogRef.close(false);
  }

  public validDocumentsInGroup(): void {
    this.server.setGroupDocumentsId(this.data.group, this.documentsIdInGroup).then(
      () => {
        // When okay, push devices
        this.server.pushDevices(this.data.group);
      }
    );
    this.dialogRef.close(true);
  }

}
