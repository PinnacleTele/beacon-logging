import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'sorter'
})
export class SorterPipe implements PipeTransform {

  transform(value: any[], direcion: string, prop?: string): any {
    if (!value) {
      return [];
    }
    if (!direcion || !prop) {
      return value
    }
    if (value.length > 0) {
      const _direction = direcion === 'asc' ? -1 : 1,
        _isArr = Array.isArray(value),
        _type = typeof value[0],
        _flag = _isArr && _type === 'object' ? true : _isArr && _type !== 'object' ? false : true;
      value.sort((a, b) => {
        if (typeof a[prop] == 'string') {
          a = _flag ? a[prop].trim().toLowerCase() : a;
          b = _flag ? b[prop].trim().toLowerCase() : b;
        }
        else {
          a = _flag ? a[prop] : a;
          b = _flag ? b[prop] : b;
        }

        if (typeof a === 'string') {
          return a > b ? -1 * _direction : 1 * _direction;
        } else if (typeof a === 'number') {
          return a - b > 0 ? -1 * _direction : 1 * _direction;
        }
      });
    }
    return value;
  }

}
