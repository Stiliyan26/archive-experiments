import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

import { GameOfLifeStore } from '../../data-access/game-of-life.store';
import { LifeGridComponent } from '../life-grid/life-grid.component';
import { GameStatusBarComponent } from '../game-status-bar/game-status-bar.component';
import { PatternSelectComponent } from '../pattern-select/pattern-select.component';
import { PatternSaveFormComponent } from '../pattern-save-form/pattern-save-form.component';
import { PatternService } from '../../data-access/pattern.service';

@Component({
  selector: 'app-game-of-life-page',
  standalone: true,
  imports: [
    LifeGridComponent,
    GameStatusBarComponent,
    PatternSelectComponent,
    PatternSaveFormComponent,
  ],
  providers: [GameOfLifeStore, PatternService],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './game-of-life-page.component.html',
  styleUrls: ['./game-of-life-page.component.css'],
})
export class GameOfLifePageComponent {
  private readonly store = inject(GameOfLifeStore);

  readonly board = this.store.board;
  readonly running = this.store.running;
}
