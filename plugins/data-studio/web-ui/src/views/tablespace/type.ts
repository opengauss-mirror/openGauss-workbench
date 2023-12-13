export interface Generateform {
  tablespaceName: string;
  owner: string;
  sequentialOverhead: string;
  nonSequentialOverhead: string;
  path: string;
  relativePath: boolean;
  maxStorage: number;
  maxSizeUnit: string;
  unlimitedSize?: boolean;
  comment: string;
}

export interface Memberform {
  role: string[];
  administrator: string[];
  belong?: string[];
}
