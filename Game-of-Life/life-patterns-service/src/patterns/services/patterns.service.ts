import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';

import { CreatePatternDto } from '../dto/create-pattern.dto';
import { PatternResponseDto } from '../dto/pattern-response.dto';
import { UpdatePatternDto } from '../dto/update-pattern.dto';
import { CellState } from '../types/cell-state.type';
import { Pattern, PatternDocument } from '../schemas/pattern.schema';

@Injectable()
export class PatternsService {
  constructor(
    @InjectModel(Pattern.name)
    private readonly patternModel: Model<PatternDocument>,
  ) {}

  async create(dto: CreatePatternDto): Promise<PatternResponseDto> {
    const grid = this.normalizeGrid(dto.grid);
    const derivedRows = grid.length;
    const derivedCols = grid[0]?.length ?? 0;

    this.assertDimensions(grid, dto.rows, dto.cols);

    const created = await this.patternModel.create({
      ...dto,
      grid,
      rows: derivedRows,
      cols: derivedCols,
    });

    return this.toResponseDto(created);
  }

  async findAll(): Promise<PatternResponseDto[]> {
    const patterns = await this.patternModel
      .find()
      .sort({ createdAt: -1 })
      .exec();

    return patterns.map((pattern) => this.toResponseDto(pattern));
  }

  async findOne(id: string): Promise<PatternResponseDto> {
    const pattern = await this.patternModel.findById(id).exec();

    if (!pattern) {
      throw new NotFoundException(
        `Pattern with identifier "${id}" was not found.`,
      );
    }

    return this.toResponseDto(pattern);
  }

  async update(id: string, dto: UpdatePatternDto): Promise<PatternResponseDto> {
    const existing = await this.patternModel.findById(id).exec();

    if (!existing) {
      throw new NotFoundException(
        `Pattern with identifier "${id}" was not found.`,
      );
    }

    const nextGrid = dto.grid
      ? this.normalizeGrid(dto.grid)
      : (existing.grid ?? []);

    const derivedRows = nextGrid.length;
    const derivedCols = nextGrid[0]?.length ?? 0;

    const nextRows = dto.rows ?? derivedRows ?? existing.rows;
    const nextCols = dto.cols ?? derivedCols ?? existing.cols;

    this.assertDimensions(nextGrid, nextRows, nextCols);

    existing.set({
      ...dto,
      grid: nextGrid,
      rows: derivedRows,
      cols: derivedCols,
    });

    const updated = await existing.save();
    return this.toResponseDto(updated);
  }

  async remove(id: string): Promise<PatternResponseDto> {
    const deleted = await this.patternModel.findByIdAndDelete(id).exec();

    if (!deleted) {
      throw new NotFoundException(
        `Pattern with identifier "${id}" was not found.`,
      );
    }

    return this.toResponseDto(deleted);
  }

  private normalizeGrid(grid: CellState[][]): CellState[][] {
    return grid.map<CellState[]>((row) =>
      row.map<CellState>((cell) => (cell ? 1 : 0)),
    );
  }

  private assertDimensions(
    grid: CellState[][],
    rows: number,
    cols: number,
  ): void {
    if (grid.length !== rows) {
      throw new BadRequestException(
        `Grid row count (${grid.length}) does not match rows (${rows}).`,
      );
    }

    const columnMismatch = grid.some((row) => row.length !== cols);
    
    if (columnMismatch) {
      throw new BadRequestException(
        'One or more grid rows do not match the specified cols.',
      );
    }
  }

  private toResponseDto(pattern: PatternDocument): PatternResponseDto {
    const plain = pattern.toObject<
      Pattern & {
        _id: Types.ObjectId;
        createdAt: Date;
        updatedAt: Date;
      }
    >();

    return {
      id: plain._id.toHexString(),
      name: plain.name,
      description: plain.description,
      rows: plain.rows,
      cols: plain.cols,
      grid: plain.grid as CellState[][],
      createdBy: plain.createdBy,
      createdAt: plain.createdAt,
      updatedAt: plain.updatedAt,
    };
  }
}
