import {
  ArrayMaxSize,
  ArrayMinSize,
  ArrayNotEmpty,
  IsArray,
  IsInt,
  IsOptional,
  IsString,
  MaxLength,
  Min,
} from 'class-validator';

import { IsValidGrid } from '../validators/is-valid-grid.decorator';
import { CellState } from '../types/cell-state.type';

export class CreatePatternDto {
  @IsString()
  @MaxLength(100)
  name!: string;

  @IsOptional()
  @IsString()
  @MaxLength(500)
  description?: string;

  @IsArray()
  @ArrayNotEmpty()
  @ArrayMinSize(1)
  @ArrayMaxSize(2000)
  @IsValidGrid()
  grid!: CellState[][];

  @IsInt()
  @Min(1)
  rows!: number;

  @IsInt()
  @Min(1)
  cols!: number;

  @IsOptional()
  @IsString()
  @MaxLength(100)
  createdBy?: string;
}
