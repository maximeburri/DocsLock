import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'devicesGroup',
  pure : false
})
export class DeviceGroupPipe implements PipeTransform {

  transform(devices: any, group?: any): any {
    return devices.filter(d => 
      (!group && !d.group) ||
      (group && d.group && (d.group.id == group.id))
    );
  }

}
