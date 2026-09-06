export type CellState = 0 | 1;

export type Board = ReadonlyArray<ReadonlyArray<CellState>>;

export interface BoardSize {
    rows: number;
    cols: number;
}

export function createRandomBoard(size: BoardSize): Board {
    return Array.from({ length: size.rows }, (_, row) =>
        Array.from({ length: size.cols }, (_, col) => {
            return Math.random() < 0.5 
                ? 0
                : 1;
        })
    ) as Board;
}

export function computeNextBoard(currentBoard: Board, size: BoardSize): Board {
    return Array.from({ length: size.rows }, (_, row) =>
        Array.from({ length: size.cols }, (_, col) => {
            const aliveNeighborCount = countLiveNeighbors(currentBoard, row, col, size);

            return nextCellState(currentBoard[row][col], aliveNeighborCount);
        })
    ) as Board;
}

const DIRECTIONS = [
    [0, 1],
    [0, -1],
    [1, 0],
    [-1, 0],
    [-1, -1],
    [-1, 1],
    [1, -1],
    [1, 1],
  ] as const;

export function countLiveNeighbors(
    board: Board,
    row: number,
    col: number,
    size: BoardSize
): number {
    let aliveNeighborCount = 0;

    for (const [dRow, dCol] of DIRECTIONS) {
        const rowModified = wrapIndex(row + dRow, size.rows);
        const colModified = wrapIndex(col + dCol, size.cols);
        
        // is alive
        if (board[rowModified][colModified]) {
            aliveNeighborCount++;
        }
    }

    return aliveNeighborCount;
}

export function nextCellState(cell: CellState, neighbors: number): CellState {
    // 1. If cell is alive
    if (cell === 1) {
        // underpopulation || overpopulation
        if (neighbors < 2 || 3 < neighbors) {
            return 0;
        }
        // lives to the next generation -> 2 <= neighbors && neighbors <= 3
        return 1;
    }
    // 2. If cell is dead
    return neighbors === 3
        ? 1
        : 0;
}

export function wrapIndex(index: number, max: number): number {
    const wrapped = index % max;

    return wrapped < 0
        ? max + wrapped
        : wrapped;
}

