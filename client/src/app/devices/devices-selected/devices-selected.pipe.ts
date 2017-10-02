import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'devicesSelected'
})
export class DevicesSelectedPipe implements PipeTransform {

  transform(devices: any, args?: any): any {
    return devices.filter(d => d.isSelected);
  }

}
