import {
  ValidationArguments,
  ValidatorConstraint,
  ValidatorConstraintInterface,
} from 'class-validator';

@ValidatorConstraint({ name: 'isValidGrid', async: false })
export class IsValidGridConstraint implements ValidatorConstraintInterface {
  validate(value: unknown): boolean {
    if (!Array.isArray(value) || value.length === 0) {
      return false;
    }

    const rowLength = Array.isArray(value[0]) ? value[0].length : 0;

    if (rowLength === 0) {
      return false;
    }

    return value.every((row) => {
      if (!Array.isArray(row) || row.length !== rowLength) {
        return false;
      }

      return row.every(
        (cell) => cell === 0 || cell === 1
      );
    });
  }

  defaultMessage(args: ValidationArguments): string {
    return `${args.property} must be a rectangular 2D array containing only boolean/0/1 values`;
  }
}
