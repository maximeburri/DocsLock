import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { EditLabelDialogComponent } from './edit-label-dialog.component';

describe('EditLabelDialogComponent', () => {
  let component: EditLabelDialogComponent;
  let fixture: ComponentFixture<EditLabelDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ EditLabelDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditLabelDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
