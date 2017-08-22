import { Route } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { DevicesComponent } from './devices/devices.component';
import { FilesComponent } from './files/files.component';
import { UserComponent } from './user/user.component';

export const MODULE_ROUTES: Route[] =[
    { path: 'devices', component: DevicesComponent },
    { path: 'files', component: FilesComponent },
    { path: 'user', component: UserComponent },
    { path: '', redirectTo: 'devices', pathMatch: 'full' }
]

export const MODULE_COMPONENTS = [
    DevicesComponent,
    FilesComponent,
    UserComponent,
]
