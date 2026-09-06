import { CellState } from '../types/cell-state.type';

export class PatternResponseDto {
  id!: string;
  name!: string;
  description?: string;
  rows!: number;
  cols!: number;
  grid!: CellState[][];
  createdBy?: string;
  createdAt!: Date;
  updatedAt!: Date;
}
