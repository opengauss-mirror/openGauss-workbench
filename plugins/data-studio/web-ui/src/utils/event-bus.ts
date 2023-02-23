class EventBus {
  static list: { [key: string]: Array<(data) => void> } = {};

  // subscribe
  static listen(name: EventTypeName, fn: (data) => void) {
    this.list[name] = this.list[name] || [];
    this.list[name].push(fn);
  }

  // release
  static notify(name: EventTypeName, data?: any) {
    if (this.list[name]) {
      this.list[name].forEach((fn: (data) => void) => {
        fn(data);
      });
    }
  }

  // cancel subscribe
  static unListen(name: EventTypeName) {
    if (this.list[name]) {
      Reflect.deleteProperty(this.list, name);
    }
  }
}
export default EventBus;

export enum EventTypeName {
  OPEN_CONNECT_DIALOG,
  OPEN_CONNECT_INFO_DIALOG,
  GET_DATABASE_LIST,
  UPDATE_DATABASE_LIST,
  CLOSE_SELECTED_TAB,
  CLOSE_ALL_TAB,
  REFRESH_DATABASE_LIST,
}
