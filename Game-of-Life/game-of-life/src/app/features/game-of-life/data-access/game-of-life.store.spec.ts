import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';

import { GameOfLifeStore } from './game-of-life.store';
import { PatternService } from './pattern.service';
import { Pattern } from '../models/pattern.models';

describe('GameOfLifeStore', () => {
  let store: GameOfLifeStore;
  let patternService: jasmine.SpyObj<PatternService>;

  const nowIso = () => new Date().toISOString();

  const createPattern = (overrides: Partial<Pattern> = {}): Pattern => ({
    id: overrides.id ?? 'pattern-1',
    name: overrides.name ?? 'Glider',
    description: overrides.description,
    rows: overrides.rows ?? 20,
    cols: overrides.cols ?? 60,
    grid: overrides.grid ?? Array.from({ length: 20 }, () => Array(60).fill(0)),
    createdBy: overrides.createdBy,
    createdAt: overrides.createdAt ?? nowIso(),
    updatedAt: overrides.updatedAt ?? nowIso(),
  });

  beforeEach(() => {
    jasmine.clock().install();

    patternService = jasmine.createSpyObj<PatternService>('PatternService', [
      'getAll',
      'getById',
      'create',
      'update',
      'delete',
    ]);
    patternService.getAll.and.returnValue(of([]));

    TestBed.configureTestingModule({
      providers: [
        GameOfLifeStore,
        { provide: PatternService, useValue: patternService },
      ],
    });

    store = TestBed.inject(GameOfLifeStore);
    store.pause();
  });

  afterEach(() => {
    jasmine.clock().uninstall();
  });

  it('should load patterns from the service', async () => {
    const mockPatterns = [createPattern({ id: 'p-1', name: 'Block' })];
    patternService.getAll.and.returnValue(of(mockPatterns));

    await store.loadPatterns();

    expect(patternService.getAll).toHaveBeenCalled();
    expect(store.patterns()).toEqual(mockPatterns);
    expect(store.patternsLoading()).toBeFalse();
    expect(store.patternsError()).toBeNull();
  });

  it('should capture load errors', async () => {
    patternService.getAll.and.returnValue(
      throwError(() => new Error('Network down')),
    );

    await store.loadPatterns();

    expect(store.patterns()).toEqual([]);
    expect(store.patternsError()).toBe('Network down');
  });

  it('should save the current pattern and refresh cache', async () => {
    const savedPattern = createPattern({ id: 'saved-1', name: 'New Pattern' });

    patternService.create.and.callFake((payload) =>
      of({
        ...payload,
        id: 'saved-1',
        createdAt: nowIso(),
        updatedAt: nowIso(),
      } as Pattern),
    );

    store.pause();
    store.updatePatternName('New Pattern');

    await store.saveCurrentPattern();

    expect(patternService.create).toHaveBeenCalled();
    const payload = patternService.create.calls.mostRecent().args[0];
    expect(payload.name).toBe('New Pattern');
    expect(payload.rows).toBe(20);
    expect(payload.cols).toBe(60);

    expect(store.patterns()[0].id).toBe('saved-1');
    expect(store.patternName()).toBe('');
    expect(store.saveStatus()).toBe('success');
    expect(store.saveMessage()).toContain('Saved "New Pattern"');

    jasmine.clock().tick(3001);
    expect(store.saveStatus()).toBe('idle');
    expect(store.saveMessage()).toBeNull();
  });

  it('should handle save errors', async () => {
    patternService.create.and.returnValue(
      throwError(
        () =>
          new HttpErrorResponse({
            status: 400,
            error: { message: ['Name already exists'] },
          }),
      ),
    );

    store.pause();
    store.updatePatternName('Duplicate');

    await store.saveCurrentPattern();

    expect(store.saveStatus()).toBe('error');
    expect(store.saveMessage()).toContain('Name already exists');
  });

  it('should apply a saved pattern to the board', () => {
    const pattern = createPattern({
      id: 'glider',
      grid: [
        [1, 0],
        [0, 1],
      ],
      rows: 2,
      cols: 2,
    });

    store.patterns.set([pattern]);
    store.applyPattern('glider');

    expect(store.running()).toBeFalse();
    expect(store.board()[0][0]).toBe(1);
    expect(store.board()[1][1]).toBe(1);
    expect(store.selectedPatternId()).toBe('glider');
  });

  it('should set an error when applying a missing pattern', () => {
    store.applyPattern('missing');

    expect(store.patternsError()).toBe('Selected pattern could not be found.');
  });
});

