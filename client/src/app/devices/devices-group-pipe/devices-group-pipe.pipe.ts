import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'devicesGroupPipe',
  pure : false
})
export class DeviceGroupPipePipe implements PipeTransform {

  transform(devices: any, group?: any): any {
    return devices.filter(d => 
      (!group && !d.group) ||
      (group && d.group && (d.group.id == group.id))
    );
  }

}
