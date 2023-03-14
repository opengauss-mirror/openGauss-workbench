class EventBus {
  static list: { [key: string]: any } = {};

  // subscribe
  static listen(name: EventTypeName, fn: any) {
    this.list[name] = this.list[name] || [];
    this.list[name].push(fn);
  }

  // release
  static notify(name: EventTypeName, ...params: any[]) {
    if (this.list[name]) {
      this.list[name].forEach((fn: any) => {
        fn(...params);
      });
    }
  }

  // cancel subscribe
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
  GET_DATABASE_LIST,
  UPDATE_DATABASE_LIST,
  CLOSE_SELECTED_TAB,
  CLOSE_ALL_TAB,
  CLOSE_ALL_TAB_TO_LAST,
  REFRESH_DATABASE_LIST,
  UPDATE_DEBUG_BUTTON,
}
