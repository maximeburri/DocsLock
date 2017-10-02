import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DevicesActionsComponent } from './devices-actions.component';

describe('DevicesActionsComponent', () => {
  let component: DevicesActionsComponent;
  let fixture: ComponentFixture<DevicesActionsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DevicesActionsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DevicesActionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
