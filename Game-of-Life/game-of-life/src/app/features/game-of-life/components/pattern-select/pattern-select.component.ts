import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { GameOfLifeStore } from '../../data-access/game-of-life.store';

@Component({
  selector: 'app-pattern-select',
  standalone: true,
  templateUrl: './pattern-select.component.html',
  styleUrls: ['./pattern-select.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatternSelectComponent {

  private readonly store = inject(GameOfLifeStore);

  readonly patterns = this.store.patterns;
  readonly loading = this.store.patternsLoading;
  readonly error = this.store.patternsError;
  readonly selectedPatternId = this.store.selectedPatternId;

  readonly hasPatterns = computed(() => this.patterns().length > 0);

  protected onSelect(value: string): void {
    if (value) {
      this.store.applyPattern(value);
    } else {
      this.store.selectedPatternId.set(null);
    }
  }

  protected refresh(): void {
    this.store.loadPatterns();
  }
}

