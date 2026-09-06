import { PartialType } from '@nestjs/mapped-types';

import { CreatePatternDto } from './create-pattern.dto';

export class UpdatePatternDto extends PartialType(CreatePatternDto) {}
