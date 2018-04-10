import { Inject, Component, OnInit } from '@angular/core';
import {MatDialog, MatDialogRef, MAT_DIALOG_DATA} from '@angular/material';
import { ServerService } from '../../common/server.service';

export type Mode = 'edit' | 'new';
export type Data = {
  mode : Mode,
  group? : any
};

@Component({
  selector: 'app-edit-group-dialog',
  templateUrl: './edit-group-dialog.component.html',
  styleUrls: ['./edit-group-dialog.component.scss']
})
export class EditGroupDialogComponent implements OnInit {
  public name: string;

  constructor(
    public dialogRef: MatDialogRef<EditGroupDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Data,
    private _server: ServerService
  ) { }

  ngOnInit() {
  }

  public newGroup(name: string) {
    this._server.newGroup(name).then( result =>
      this.dialogRef.close(result)
    )
  }

  public editGroup(name: string) {
    this.data.group.name = name;
    this._server.setGroup(this.data.group).then( result =>
      this.dialogRef.close(result)
    )
  }

  public onNoClick(): void {
    this.dialogRef.close();
  }
}
