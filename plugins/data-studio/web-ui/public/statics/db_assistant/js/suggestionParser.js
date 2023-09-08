/* eslint-disable no-undef */
function Commands() {
  this.cmds = [];
  this.add = add;
  this.find = find;
  this.findInArray = findInArray;
  this.getOneFromArray = getOneFromArray;
  this.load = load;
  var _this = this;
  function add(cmd) {
    this.cmds[this.cmds.length] = cmd;
  }
  function find(cmdId, curPos) {
    var tmpCmds = [];
    $(this.cmds).each(function (i, cmd) {
      if (cmd.id.split(' ')[curPos] == cmdId) {
        tmpCmds[tmpCmds.length] = cmd;
      }
    });
    return tmpCmds;
  }
  function findInArray(array, cmdId, curPos) {
    var tmpCmds = [];
    $(array).each(function (i, cmd) {
      if (cmd.id.split(' ')[curPos] == cmdId) {
        tmpCmds[tmpCmds.length] = cmd;
      }
    });
    if (tmpCmds.length == 0) {
      tmpCmds = array;
    }
    return tmpCmds;
  }
  function getOneFromArrayCnt(array, curPos) {
    var cc = 0;
    $(array).each(function (i, cmd) {
      if (cmd.id.split(' ').length == curPos) {
        cc++;
      }
    });
    return cc;
  }
  function getOneFromArray(array, curPos) {
    var rtnCmd = '';
    var tempCnt = getOneFromArrayCnt(array, curPos);
    $(array).each(function (i, cmd) {
      if (
        cmd.id.split(' ').length == curPos &&
        (tempCnt == 1 ||
          tempCnt > 2 ||
          (tempCnt == 2 && cmd.alias.replace(/\s+/g, '') == selectedCmdAlias.replace(/\s+/g, '')))
      ) {
        rtnCmd = cmd;
      }
    });
    return rtnCmd;
  }
  function load(xml) {
    _this.cmds.length = 0;
    $(xml)
      .find('command')
      .each(function (i, cmdXml) {
        var cmd = new Command(
          cmdXml.getAttribute('id'),
          cmdXml.getAttribute('alias'),
          $(cmdXml).children('suggestion')[0].textContent,
        );
        $(cmdXml)
          .children('sub')
          .each(function (j, subXml) {
            var sub = new Sub($(subXml).attr('id'), $(subXml).children('suggestion').text());
            cmd.addSub(sub);
          });
        _this.add(cmd);
      });
  }
}

function fillSub(sub, subXml) {
  $(subXml)
    .children('option')
    .each(function (i, optionXml) {
      var option = new Option($(optionXml).attr('id'), $(optionXml).children('suggestion').text());
      sub.addOption(option);
    });
  $(subXml)
    .children('sub')
    .each(function (i, subXmlTemp) {
      var subTemp = new Sub($(subXmlTemp).attr('id'), $(subXmlTemp).children('suggestion').text());
      fillSub(subTemp, subXmlTemp);
      sub.addSub(subTemp);
    });
}
function Command(id, alias, suggestion) {
  this.id = id;
  this.alias = alias;
  this.suggestion = suggestion;
  this.options = [];
  this.subs = [];
  this.addOption = addOption;
  this.findOption = findOption;
  this.addSub = addSub;
  this.findSub = findSub;

  function addOption(option) {
    this.options[this.options.length] = option;
  }
  function findOption(optionId) {
    var tmpOption = '';
    $(this.options).each(function (i, option) {
      if (optionId == option.id) {
        tmpOption = option;
      }
    });
    return tmpOption;
  }

  function addSub(sub) {
    this.subs[this.subs.length] = sub;
  }
  function findSub(subId) {
    var tmpSub = '';
    $(this.subs).each(function (i, sub) {
      if (subId == sub.id) {
        tmpSub = sub;
      }
    });
    return tmpSub;
  }
}

function Option(id, suggestion) {
  this.id = id;
  this.suggestion = suggestion;
}

function Sub(id, suggestion) {
  this.id = id;
  this.suggestion = suggestion;
  this.options = [];
  this.subs = [];
  this.addOption = addOption;
  this.findOption = findOption;
  this.addSub = addSub;
  this.findSub = findSub;

  function addOption(option) {
    this.options[this.options.length] = option;
  }
  function findOption(optionId) {
    var tmpOption = '';
    $(this.options).each(function (i, option) {
      if (optionId == option.id) {
        tmpOption = option;
      }
    });
    return tmpOption;
  }

  function addSub(sub) {
    this.subs[this.subs.length] = sub;
  }
  function findSub(subId) {
    var tmpSub = '';
    $(this.subs).each(function (i, sub) {
      if (subId == sub.id) {
        tmpSub = sub;
      }
    });
    return tmpSub;
  }
}
