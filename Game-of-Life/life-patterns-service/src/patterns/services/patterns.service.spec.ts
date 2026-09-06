import { Test, TestingModule } from '@nestjs/testing';
import {
  getConnectionToken,
  getModelToken,
  MongooseModule,
} from '@nestjs/mongoose';
import { Connection, Model } from 'mongoose';
import { MongoMemoryServer } from 'mongodb-memory-server';

import { CreatePatternDto } from '../dto/create-pattern.dto';
import { UpdatePatternDto } from '../dto/update-pattern.dto';
import { Pattern, PatternDocument, PatternSchema } from '../schemas/pattern.schema';
import { PatternsService } from './patterns.service';

const createDto = (overrides: Partial<CreatePatternDto> = {}): CreatePatternDto => ({
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
});

describe('PatternsService', () => {
  let moduleRef: TestingModule;
  let service: PatternsService;
  let model: Model<PatternDocument>;
  let mongo: MongoMemoryServer;
  let connection: Connection;

  beforeAll(async () => {
    mongo = await MongoMemoryServer.create();

    moduleRef = await Test.createTestingModule({
      imports: [
        MongooseModule.forRoot(mongo.getUri(), {
          dbName: 'patterns-service-tests',
        }),
        MongooseModule.forFeature([{ name: Pattern.name, schema: PatternSchema }]),
      ],
      providers: [PatternsService],
    }).compile();

    service = moduleRef.get(PatternsService);
    model = moduleRef.get<Model<PatternDocument>>(getModelToken(Pattern.name));
    connection = moduleRef.get<Connection>(getConnectionToken());
  });

  afterEach(async () => {
    await model.deleteMany({});
  });

  afterAll(async () => {
    await connection.close();
    await moduleRef.close();
    await mongo.stop();
  });

  it('creates a pattern with normalized grid values', async () => {
    const dto = createDto({
      grid: [
        [true, false, 1],
        [0, true, false],
      ],
    });

    const result = await service.create(dto);

    expect(result.id).toBeDefined();
    expect(result.grid).toEqual([
      [1, 0, 1],
      [0, 1, 0],
    ]);
    expect(result.rows).toBe(2);
    expect(result.cols).toBe(3);

    const saved = await model.findById(result.id).lean();
    expect(saved?.grid).toEqual([
      [1, 0, 1],
      [0, 1, 0],
    ]);
  });

  it('throws when provided dimensions do not match the grid', async () => {
    await expect(
      service.create(
        createDto({
          rows: 3,
        }),
      ),
    ).rejects.toMatchObject({
      message: expect.stringContaining('Grid row count'),
    });
  });

  it('returns patterns sorted by createdAt descending', async () => {
    await service.create(createDto({ name: 'First' }));
    await new Promise((resolve) => setTimeout(resolve, 5));
    await service.create(createDto({ name: 'Second' }));

    const results = await service.findAll();

    expect(results).toHaveLength(2);
    expect(results[0].name).toBe('Second');
    expect(results[1].name).toBe('First');
  });

  it('finds a pattern by id', async () => {
    const created = await service.create(createDto());

    const fetched = await service.findOne(created.id);

    expect(fetched.id).toBe(created.id);
  });

  it('throws when pattern is not found', async () => {
    await expect(service.findOne('66f6e7bd7dd84d403c1f4f1a')).rejects.toMatchObject({
      status: 404,
    });
  });

  it('updates an existing pattern and normalizes input', async () => {
    const created = await service.create(createDto({ name: 'Original' }));

    const updatePayload: UpdatePatternDto = {
      name: 'Updated',
      grid: [
        [false, true],
        [true, false],
      ],
      rows: 2,
      cols: 2,
    };

    const updated = await service.update(created.id, updatePayload);

    expect(updated.name).toBe('Updated');
    expect(updated.grid).toEqual([
      [0, 1],
      [1, 0],
    ]);
    expect(updated.rows).toBe(2);
    expect(updated.cols).toBe(2);
  });

  it('throws when updating a missing pattern', async () => {
    await expect(
      service.update('66f6e7bd7dd84d403c1f4f1a', {
        name: 'Missing',
      }),
    ).rejects.toMatchObject({
      status: 404,
    });
  });

  it('removes a pattern by id', async () => {
    const created = await service.create(createDto());

    const deleted = await service.remove(created.id);

    expect(deleted.id).toBe(created.id);
    expect(await model.countDocuments()).toBe(0);
  });

  it('throws when removing a missing pattern', async () => {
    await expect(service.remove('66f6e7bd7dd84d403c1f4f1a')).rejects.toMatchObject({
      status: 404,
    });
  });
});
