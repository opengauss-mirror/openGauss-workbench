import { RouteLocationNormalized, RouteRecord } from 'vue-router';

export interface VisitedView extends RouteLocationNormalized {
  name: string;
  id: number;
  title: string;
  showName?: string;
  connectInfoName?: string;
  dbname?: string;
  terminalNum?: string;
  isReload: boolean;
  loadTime: number;
  isEdit: boolean;
  matched?: RouteRecord[];
}
