export interface Pattern {
  id: string;
  name: string;
  description?: string;
  rows: number;
  cols: number;
  grid: number[][];
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreatePatternPayload {
  name: string;
  description?: string;
  rows: number;
  cols: number;
  grid: number[][];
  createdBy?: string;
}


