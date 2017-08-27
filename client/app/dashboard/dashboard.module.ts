import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MODULE_COMPONENTS, MODULE_ROUTES } from './dashboard.routes';
import { CommonModule } from '@angular/common';

@NgModule({
    imports: [
        RouterModule.forChild(MODULE_ROUTES),
        CommonModule
    ],
    declarations: [ MODULE_COMPONENTS ]
})

export class DashboardModule{}
