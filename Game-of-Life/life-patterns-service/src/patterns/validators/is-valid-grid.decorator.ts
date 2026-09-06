import { applyDecorators } from '@nestjs/common';
import { Validate } from 'class-validator';

import { IsValidGridConstraint } from './is-valid-grid.constraint';

export const IsValidGrid = () =>
  applyDecorators(Validate(IsValidGridConstraint));
