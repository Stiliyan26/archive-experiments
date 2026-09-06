import { Test, TestingModule } from '@nestjs/testing';

import { CreatePatternDto } from '../dto/create-pattern.dto';
import { PatternResponseDto } from '../dto/pattern-response.dto';
import { UpdatePatternDto } from '../dto/update-pattern.dto';
import { PatternsController } from './patterns.controller';
import { PatternsService } from '../services/patterns.service';

describe('PatternsController', () => {
  let controller: PatternsController;
  let service: jest.Mocked<PatternsService>;

  const responseFactory = (overrides: Partial<PatternResponseDto> = {}): PatternResponseDto => ({
    id: overrides.id ?? 'pattern-1',
    name: overrides.name ?? 'Glider',
    description: overrides.description,
    rows: overrides.rows ?? 2,
    cols: overrides.cols ?? 3,
    grid:
      overrides.grid ?? [
        [1, 0, 1],
        [0, 1, 0],
      ],
    createdBy: overrides.createdBy,
    createdAt: overrides.createdAt ?? new Date(),
    updatedAt: overrides.updatedAt ?? new Date(),
  });

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [PatternsController],
      providers: [
        {
          provide: PatternsService,
          useValue: {
            create: jest.fn(),
            findAll: jest.fn(),
            findOne: jest.fn(),
            update: jest.fn(),
            remove: jest.fn(),
          },
        },
      ],
    }).compile();

    controller = module.get(PatternsController);
    service = module.get(PatternsService) as jest.Mocked<PatternsService>;
  });

  it('delegates create to PatternsService', async () => {
    const dto: CreatePatternDto = {
      name: 'Spaceship',
      rows: 2,
      cols: 3,
      grid: [
        [1, 0, 1],
        [0, 1, 0],
      ],
    };
    const expected = responseFactory({ name: 'Spaceship' });
    service.create.mockResolvedValue(expected);

    const result = await controller.create(dto);

    expect(service.create).toHaveBeenCalledWith(dto);
    expect(result).toBe(expected);
  });

  it('returns all patterns', async () => {
    const expected = [responseFactory({ id: 'pattern-1' })];
    service.findAll.mockResolvedValue(expected);

    const result = await controller.findAll();

    expect(service.findAll).toHaveBeenCalled();
    expect(result).toBe(expected);
  });

  it('returns a pattern by id', async () => {
    const expected = responseFactory({ id: 'pattern-42' });
    service.findOne.mockResolvedValue(expected);

    const result = await controller.findOne('pattern-42');

    expect(service.findOne).toHaveBeenCalledWith('pattern-42');
    expect(result).toBe(expected);
  });

  it('updates an existing pattern', async () => {
    const dto: UpdatePatternDto = { name: 'Updated' };
    const expected = responseFactory({ name: 'Updated' });
    service.update.mockResolvedValue(expected);

    const result = await controller.update('pattern-1', dto);

    expect(service.update).toHaveBeenCalledWith('pattern-1', dto);
    expect(result).toBe(expected);
  });

  it('removes a pattern', async () => {
    const expected = responseFactory({ id: 'pattern-1' });
    service.remove.mockResolvedValue(expected);

    const result = await controller.remove('pattern-1');

    expect(service.remove).toHaveBeenCalledWith('pattern-1');
    expect(result).toBe(expected);
  });
});
