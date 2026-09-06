import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { GameOfLifeStore } from '../../data-access/game-of-life.store';

@Component({
  selector: 'app-pattern-save-form',
  standalone: true,
  templateUrl: './pattern-save-form.component.html',
  styleUrls: ['./pattern-save-form.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatternSaveFormComponent {

  private readonly store = inject(GameOfLifeStore);

  readonly patternName = this.store.patternName;
  readonly canSave = this.store.canSavePattern;
  readonly isSaving = this.store.isSaving;
  readonly status = this.store.saveStatus;
  readonly message = this.store.saveMessage;

  readonly statusIsError = computed(() => this.status() === 'error');

  protected onInput(value: string): void {
    this.store.updatePatternName(value);
  }

  protected save(): void {
    void this.store.saveCurrentPattern();
  }
}

