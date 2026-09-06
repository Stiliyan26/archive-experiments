import { ChangeDetectionStrategy, Component, computed, input } from '@angular/core';

import { Board } from '../../utils/grid-utils';

@Component({
  selector: 'app-life-grid',
  standalone: true,
  templateUrl: './life-grid.component.html',
  styleUrls: ['./life-grid.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class LifeGridComponent {
  readonly board = input.required<Board>();

  readonly columnCount = computed(() => this.board()?.[0]?.length ?? 0);

  get gridTemplateColumns(): string {
    const cols = this.columnCount();

    return cols > 0 ? `repeat(${cols}, var(--cell-size))` : 'none';
  }
}

