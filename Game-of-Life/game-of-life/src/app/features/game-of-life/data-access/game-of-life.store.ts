import {
  Injectable,
  effect,
  signal,
  computed,
  inject,
  DestroyRef,
} from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { GAME_CONFIG } from '../../../core/game-config/game-config';
import {
  Board,
  createRandomBoard,
  computeNextBoard,
} from '../utils/grid-utils';
import { Pattern, CreatePatternPayload } from '../models/pattern.models';

export type SaveStatus = 'idle' | 'saving' | 'success' | 'error';
import { PatternService } from './pattern.service';

@Injectable()
export class GameOfLifeStore {
  
  private readonly size = {
    rows: GAME_CONFIG.rows,
    cols: GAME_CONFIG.cols,
  };

  private readonly destroyRef = inject(DestroyRef);
  private readonly patternService = inject(PatternService);

  private timerId: ReturnType<typeof setInterval> | null = null;
  private saveStatusResetTimer: ReturnType<typeof setTimeout> | null = null;

  readonly board = signal<Board>(createRandomBoard(this.size));
  readonly generation = signal<number>(0);
  readonly running = signal<boolean>(true);

  readonly patterns = signal<Pattern[]>([]);
  readonly patternsLoading = signal<boolean>(false);
  readonly patternsError = signal<string | null>(null);

  readonly patternName = signal<string>('');
  readonly selectedPatternId = signal<string | null>(null);

  readonly saveStatus = signal<SaveStatus>('idle');
  readonly saveMessage = signal<string | null>(null);

  readonly isSaving = computed(() => this.saveStatus() === 'saving');
  readonly canSavePattern = computed(
    () => this.patternName().trim().length > 0 && !this.isSaving()
  );

  constructor() {
    effect(() => {
      this.running()
        ? this.startTimer()
        : this.stopTimer();
    });

    this.loadPatterns();

    this.destroyRef.onDestroy(() => {
      this.stopTimer();
      
      if (this.saveStatusResetTimer) {
        clearTimeout(this.saveStatusResetTimer);
        this.saveStatusResetTimer = null;
      }
    });
  }

  randomize(): void {
    this.board.set(createRandomBoard(this.size));
    this.generation.set(0);
  }

  advance(): void {
    this.board.set(computeNextBoard(this.board(), this.size));
    this.generation.update(genCnt => genCnt + 1);
  }

  pause(): void {
    this.running.set(false);
  }

  resume(): void {
    this.running.set(true);
  }

  toggle(): void {
    this.running.update(isRunning => !isRunning);
  }

  stepOnce(): void {
    this.pause();
    this.advance();
  }

  updatePatternName(name: string): void {
    this.patternName.set(name);
  }

  async loadPatterns(): Promise<void> {
    this.patternsLoading.set(true);

    this.patternsError.set(null);
    
    try {
      // Observable to Promise
      const patterns = await firstValueFrom(this.patternService.getAll());
      this.patterns.set(patterns);
    } catch (error) {
      this.patternsError.set(this.extractErrorMessage(error));
    
    } finally {
      this.patternsLoading.set(false);
    }
  }

  async saveCurrentPattern(): Promise<void> {
    if (!this.canSavePattern()) {
      return;
    }

    this.setSaveStatus('saving');

    const payload: CreatePatternPayload = {
      name: this.patternName().trim(),
      rows: this.size.rows,
      cols: this.size.cols,
      grid: this.toMutableGrid(this.board()),
      createdBy: 'web-client',
    };

    try {
      const saved = await firstValueFrom(this.patternService.create(payload));

      this.patterns.update((patterns) => {
        return [
          saved,
          ...patterns,
        ].sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime());
      });

      this.selectedPatternId.set(saved.id);
      this.patternName.set('');
      this.setSaveStatus('success', `Saved "${saved.name}"`);
    } catch (error) {
      this.setSaveStatus('error', this.extractErrorMessage(error));
    }
  }

  applyPattern(patternId: string): void {
    const pattern = this.patterns().find((item) => item.id === patternId);

    if (!pattern) {
      this.patternsError.set('Selected pattern could not be found.');
      return;
    }

    this.pause();
    this.board.set(this.toBoard(pattern.grid));
    this.generation.set(0);
    this.selectedPatternId.set(pattern.id);
  }

  private startTimer(): void {
    this.stopTimer();
    this.timerId = setInterval(() => this.advance(), GAME_CONFIG.tickMs);
  }

  private stopTimer(): void {
    if (this.timerId !== null) {
      clearInterval(this.timerId);
      this.timerId = null;
    }
  }

  private toMutableGrid(board: Board): number[][] {
    return board.map((row) => row.map((cell) => (cell ? 1 : 0)));
  }

  private toBoard(grid: number[][]): Board {
    // If we have true and false given back from the backend
    return grid.map((row) => row.map((cell) => (cell ? 1 : 0))) as Board;
  }

  private extractErrorMessage(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim().length > 0) {
        return error.error;
      }

      if (error.error && typeof error.error === 'object' && 'message' in error.error) {
        const message = Array.isArray(error.error.message)
          ? error.error.message.join(', ')
          : error.error.message;

        if (typeof message === 'string' && message.trim().length > 0) {
          return message;
        }
      }

      if (error.message) {
        return error.message;
      }

      return `Request failed with status ${error.status}`;
    }

    else if (error instanceof Error && error.message) {
      return error.message;
    }

    return 'Something went wrong. Please try again.';
  }

  private setSaveStatus(
    status: SaveStatus,
    message: string | null = null,
  ): void {
    this.saveStatus.set(status);
    this.saveMessage.set(message);

    if (this.saveStatusResetTimer) {
      clearTimeout(this.saveStatusResetTimer);
      this.saveStatusResetTimer = null;
    }

    if (status !== 'saving') {
      this.saveStatusResetTimer = setTimeout(() => {
        this.saveStatus.set('idle');
        this.saveMessage.set(null);
      }, 3000);
    }
  }
}

