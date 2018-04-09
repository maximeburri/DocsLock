import { Inject, Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import { ServerService } from '../../common/server.service';


@Component({
  selector: 'confirmation-dialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss']
})
export class ConfirmationDialogComponent implements OnInit {
  public title : string;
  constructor(
    public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _server: ServerService
  ) { }

  ngOnInit() {
  }

  public cancel() {
    this.dialogRef.close(false);
  }

  public continue(): void {
    this.dialogRef.close(true);
  }
}
