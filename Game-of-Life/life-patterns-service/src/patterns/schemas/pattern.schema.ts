import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

import { CellState } from '../types/cell-state.type';

export type PatternDocument = HydratedDocument<Pattern>;

@Schema({ timestamps: true })
export class Pattern {
  @Prop({ required: true, trim: true, maxlength: 100 })
  name!: string;

  @Prop({ trim: true, maxlength: 500 })
  description?: string;

  @Prop({ required: true, min: 1, max: 2000 })
  rows!: number;

  @Prop({ required: true, min: 1, max: 2000 })
  cols!: number;

  @Prop({
    type: [[Number]],
    required: true,
    validate: {
      validator: (grid: CellState[][]) => {
        if (!Array.isArray(grid) || grid.length === 0) {
          return false;
        }

        const rowLength = grid[0]?.length ?? 0;

        if (rowLength === 0) {
          return false;
        }

        return grid.every(
          (row) =>
            Array.isArray(row) &&
            row.length === rowLength &&
            row.every((cell) => cell === 0 || cell === 1),
        );
      },
      message: 'grid must be a rectangular 2D array composed of 0 and 1 values',
    },
  })
  grid!: CellState[][];

  @Prop({ trim: true, maxlength: 100 })
  createdBy?: string;
}

export const PatternSchema = SchemaFactory.createForClass(Pattern);
