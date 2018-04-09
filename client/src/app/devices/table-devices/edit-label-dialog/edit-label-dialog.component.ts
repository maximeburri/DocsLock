import { Inject, Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import { ServerService } from '../../../common/server.service';


@Component({
  selector: 'app-edit-label-dialog',
  templateUrl: './edit-label-dialog.component.html',
  styleUrls: ['./edit-label-dialog.component.scss']
})
export class EditLabelDialogComponent implements OnInit {
  public name: string;

  constructor(
    public dialogRef: MatDialogRef<EditLabelDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _server: ServerService
  ) { }

  ngOnInit() {
  }

  public onOk(label: string) {
    this._server.setDeviceLabel(this.data.device, label).then( result =>
      this.dialogRef.close(label)
    )
  }

  public onNoClick(): void {
    this.dialogRef.close();
  }
}
