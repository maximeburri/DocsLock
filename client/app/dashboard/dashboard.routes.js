"use strict";
var devices_component_1 = require('./devices/devices.component');
var files_component_1 = require('./files/files.component');
var user_component_1 = require('./user/user.component');
exports.MODULE_ROUTES = [
    { path: 'devices', component: devices_component_1.DevicesComponent },
    { path: 'files', component: files_component_1.FilesComponent },
    { path: 'user', component: user_component_1.UserComponent },
    { path: '', redirectTo: 'devices', pathMatch: 'full' }
];
exports.MODULE_COMPONENTS = [
    devices_component_1.DevicesComponent,
    files_component_1.FilesComponent,
    user_component_1.UserComponent,
];
//# sourceMappingURL=dashboard.routes.js.map