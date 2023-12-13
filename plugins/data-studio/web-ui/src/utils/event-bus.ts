class EventBus {
  static list: { [key: string]: any } = {};

  static listen(name: EventTypeName, fn: any) {
    this.list[name] = this.list[name] || [];
    this.list[name].push(fn);
  }

  static async notify(name: EventTypeName, ...params: any[]) {
    if (this.list[name]) {
      for (let i = 0; i < this.list[name].length; i++) {
        await this.list[name][i](...params);
      }
    }
  }

  static unListen(name: EventTypeName, fn?: any) {
    if (this.list[name]) {
      if (fn) {
        const index = this.list[name].indexOf(fn);
        this.list[name].splice(index, 1);
      } else {
        Reflect.deleteProperty(this.list, name);
      }
    }
  }
}
export default EventBus;

export enum EventTypeName {
  OPEN_CONNECT_DIALOG,
  OPEN_CONNECT_INFO_DIALOG,
  GET_CONNECTION_LIST,
  UPDATE_CONNECTION_LIST,
  UPDATE_CONNECTINFO,
  CLOSE_SELECTED_TAB,
  CLOSE_ALL_TAB,
  CLOSE_ALL_TAB_TO_LAST,
  REFRESH_DATABASE_LIST,
  UPDATE_DEBUG_BUTTON,
  REFRESH_ASIDER,
  OPEN_ENTERPASSWORD,
}
