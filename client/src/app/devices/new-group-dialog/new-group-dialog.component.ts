import { Inject, Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import { ServerService } from '../../common/server.service';


@Component({
  selector: 'app-new-group-dialog',
  templateUrl: './new-group-dialog.component.html',
  styleUrls: ['./new-group-dialog.component.scss']
})
export class NewGroupDialogComponent implements OnInit {
  public name: string;

  constructor(
    public dialogRef: MatDialogRef<NewGroupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
    private _server: ServerService
  ) { }

  ngOnInit() {
  }

  public newGroup(name: string) {
    this._server.newGroup(name).then( result =>
      this.dialogRef.close(result)
    )
  }

  public onNoClick(): void {
    this.dialogRef.close();
  }
}
