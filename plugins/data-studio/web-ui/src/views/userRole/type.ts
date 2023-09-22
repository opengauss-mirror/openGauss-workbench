export interface Generateform {
  name: string;
  oid: string;
  type: string;
  password: string;
  confirmPassword: string;
  beginDate: string;
  endDate: string;
  connectionLimit: number;
  resourcePool: string;
  power: string[];
  comment: string;
}

export interface Memberform {
  role: string[];
  administrator: string[];
}
