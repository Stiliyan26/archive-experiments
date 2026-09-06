import {
  Body,
  Controller,
  Delete,
  Get,
  Param,
  Post,
  Put,
} from '@nestjs/common';

import { CreatePatternDto } from '../dto/create-pattern.dto';
import { PatternResponseDto } from '../dto/pattern-response.dto';
import { UpdatePatternDto } from '../dto/update-pattern.dto';
import { PatternsService } from '../services/patterns.service';

@Controller('patterns')
export class PatternsController {
  constructor(private readonly patternsService: PatternsService) {}

  @Post()
  create(@Body() dto: CreatePatternDto): Promise<PatternResponseDto> {
    return this.patternsService.create(dto);
  }

  @Get()
  findAll(): Promise<PatternResponseDto[]> {
    return this.patternsService.findAll();
  }

  @Get(':id')
  findOne(@Param('id') id: string): Promise<PatternResponseDto> {
    return this.patternsService.findOne(id);
  }

  @Put(':id')
  update(
    @Param('id') id: string,
    @Body() dto: UpdatePatternDto,
  ): Promise<PatternResponseDto> {
    return this.patternsService.update(id, dto);
  }

  @Delete(':id')
  remove(@Param('id') id: string): Promise<PatternResponseDto> {
    return this.patternsService.remove(id);
  }
}
