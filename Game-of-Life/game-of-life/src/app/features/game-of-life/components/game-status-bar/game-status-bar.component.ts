import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';

import { GameOfLifeStore } from '../../data-access/game-of-life.store';

@Component({
  selector: 'app-game-status-bar',
  standalone: true,
  templateUrl: './game-status-bar.component.html',
  styleUrls: ['./game-status-bar.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GameStatusBarComponent {

  private readonly store = inject(GameOfLifeStore);

  readonly generation = this.store.generation;
  readonly running = this.store.running;

  readonly toggle = () => this.store.toggle();
  readonly step = () => this.store.stepOnce();
  readonly randomize = () => this.store.randomize();

  readonly statusText = computed(() => (this.running() ? 'Running' : 'Paused'));
}

