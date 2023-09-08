/* eslint-disable no-undef */
var recordPath = getCookie('DS_recordDataPath') || '/openGauss1.0.0/zh';
var datajspath = "<script language=javascript src='" + recordPath + "/data.js'></script>";
document.write(datajspath);
var isNotSingle = true;
var selectedCmdAlias = 'temp';
var formerCmd = '';
var curCmd = '';
var NameSnext;

function isHidden(oDiv) {
  var vDiv = oDiv.nextSibling;
  while (vDiv.nodeType == 3) {
    vDiv = vDiv.nextSibling;
  }
  vDiv.style.display = vDiv.style.display == 'none' ? 'block' : 'none';
  var pngs = oDiv.getElementsByTagName('img');
  switch (pngs[1].style.display) {
    case 'none':
      pngs[0].style.display = 'none';
      pngs[1].style.display = 'inline';
      break;
    case 'inline':
      pngs[0].style.display = 'inline';
      pngs[1].style.display = 'none';
      break;
  }
}

function LoadSuggestion(recordPath) {
  commands = new Commands();

  var xmlhttp = new XMLHttpRequest();
  xmlhttp.open('GET', recordPath + '/suggestion.xml', false);
  xmlhttp.send();
  var xml = xmlhttp.responseXML;

  commands.load(xml);
  getSQLHtmlForConent(commands.cmds);
}

function getSQLHtmlForConent(tempValues) {
  $.each(tempValues, function (n, v) {
    if (v.alias == NameSnext || NameSnext == v.id) {
      processInput(NameSnext);
    }
  });
}

function processInput(tempId) {
  tempId = $.trim(tempId);
  if ('' != tempId) {
    var tempIds = tempId.split(/;/);
    tempId = tempIds[tempIds.length - 1];
    if ('' == tempId || '$$' == tempId) tempId = tempIds[tempIds.length - 2];
    var tokens = StringReader(tempId);
    var tempCmds = [];
    var curPos = 0;
    for (var li = 0; li < tokens.length; li++) {
      if (tempCmds.length == 0) {
        tempCmds = commands.find(tokens[li].toUpperCase(), curPos);
        if (tempCmds.length != 0) {
          curPos++;
        }
      } else {
        var formmerA = tempCmds;
        tempCmds = commands.findInArray(tempCmds, tokens[li].toUpperCase(), curPos);
        if (tempCmds.length != 0 && formmerA != tempCmds) {
          curPos++;
        }
      }
    }
    if (tempCmds.length > 0) {
      var tempSuggestion = '';
      if (tempCmds.length == 1) {
        curCmd = tempCmds[0];
        if (curCmd.suggestion != '') {
          tempSuggestion =
            "<h4 class='sectionCmdTitle'>" +
            curCmd.alias +
            '</h4>' +
            curCmd.suggestion +
            tempSuggestion;
          if (curCmd.subs.length > 0) {
            var tempSubSuggestion = '';
            $(curCmd.subs).each(function (i, sub) {
              {
                var tempAllKeywords = '.*?' + sub.id.replace(/\s/g, '.*?');
                var reg = new RegExp(tempAllKeywords, 'gi');
                if (reg.test(tempId) && '' != sub.suggestion) {
                  tempSubSuggestion = sub.suggestion + tempSubSuggestion;
                }
              }
            });
            if (tempSubSuggestion != '')
              tempSuggestion =
                '<p class="sectionSugTitle">Suggestion</p>' +
                tempSubSuggestion +
                "<hr class='hrStyle'>" +
                tempSuggestion;
          }
        }
      } else {
        curCmd = commands.getOneFromArray(tempCmds, curPos);

        if (curCmd != '') {
          tempSuggestion =
            "<h4 class='sectionCmdTitle'>" +
            curCmd.alias +
            '</h4>' +
            curCmd.suggestion +
            tempSuggestion;
          if (curCmd.subs.length > 0) {
            var tempSubSuggestion = '';
            $(curCmd.subs).each(function (i, sub) {
              {
                var tempAllKeywords = '.*?' + sub.id.replace(/\s/g, '.*?');
                var reg = new RegExp(tempAllKeywords, 'gi');
                if (reg.test(tempId) && '' != sub.suggestion) {
                  tempSubSuggestion = sub.suggestion + tempSubSuggestion;
                }
              }
            });
            if (tempSubSuggestion != '')
              tempSuggestion =
                '<p class="sectionSugTitle">Suggestion</p>' +
                tempSubSuggestion +
                "<hr class='hrStyle'>" +
                tempSuggestion;
          }
        } else {
          backString = tempId;
          var num = 0;
          $.each(tempCmds, function (n, v) {
            curCmd = tempCmds[num];
            if (v.alias.indexOf('|') > -1) {
              if (v.alias == NameSnext) {
                tempSuggestion =
                  "<h4 class='sectionCmdTitle'>" +
                  curCmd.alias +
                  '</h4>' +
                  curCmd.suggestion +
                  tempSuggestion;
                if (curCmd.subs.length > 0) {
                  var tempSubSuggestion = '';
                  $(curCmd.subs).each(function (i, sub) {
                    {
                      var tempAllKeywords = '.*?' + sub.id.replace(/\s/g, '.*?');
                      var reg = new RegExp(tempAllKeywords, 'gi');
                      if (reg.test(tempId) && '' != sub.suggestion) {
                        tempSubSuggestion = sub.suggestion + tempSubSuggestion;
                      }
                    }
                  });
                  if (tempSubSuggestion != '')
                    tempSuggestion =
                      '<p class="sectionSugTitle">Suggestion</p>' +
                      tempSubSuggestion +
                      "<hr class='hrStyle'>" +
                      tempSuggestion;
                }
              } else {
                num++;
              }
            } else {
              if (v.alias == NameSnext) {
                tempSuggestion =
                  "<h4 class='sectionCmdTitle'>" +
                  curCmd.alias +
                  '</h4>' +
                  curCmd.suggestion +
                  tempSuggestion;
                if (curCmd.subs.length > 0) {
                  var tempSubSuggestion = '';
                  $(curCmd.subs).each(function (i, sub) {
                    {
                      var tempAllKeywords = '.*?' + sub.id.replace(/\s/g, '.*?');
                      var reg = new RegExp(tempAllKeywords, 'gi');
                      if (reg.test(tempId) && '' != sub.suggestion) {
                        tempSubSuggestion = sub.suggestion + tempSubSuggestion;
                      }
                    }
                  });
                  if (tempSubSuggestion != '')
                    tempSuggestion =
                      '<p class="sectionSugTitle">Suggestion</p>' +
                      tempSubSuggestion +
                      "<hr class='hrStyle'>" +
                      tempSuggestion;
                }
              } else {
                num++;
              }
            }
          });
          formerCmd = '';
        }
      }
      if (curCmd != '' && formerCmd == '') {
        $('#divSuggestion').html(tempSuggestion);
        $(document).scrollTop(0);
        assistantHighlight('assistantExamples');
        formerCmd = curCmd;
      } else if (curCmd != '' && formerCmd != '' && curCmd.alias != formerCmd.alias) {
        $('#divSuggestion').html(tempSuggestion);
        $(document).scrollTop(0);
        assistantHighlight('assistantExamples');
        formerCmd = curCmd;
      } else if (formerCmd == '') {
        $('#divSuggestion').html(tempSuggestion);
        $(document).scrollTop(0);
      }
    }
  }
}

function getParams(key) {
  var reg = new RegExp('(^|&)' + key + '=([^&]*)(&|$)');
  var r = window.location.search.substr(1).match(reg);
  if (r != null) {
    return unescape(r[2]);
  }
  return null;
}
$(function () {
  $('#grammarNext').click(function () {
    location.href = 'index_zh.html';
    $(this).addClass('active');
    $('#operationListNext').removeClass('active');
    $('#beginnerGuideNext').removeClass('active');
    $('#caseNext').removeClass('active');
  });
  $('#beginnerGuideNext').click(function () {
    $(this).addClass('active');
    $('#operationListNext').removeClass('active');
    $('#grammarNext').removeClass('active');
    $('#caseNext').removeClass('active');
  });

  $('#caseNext').click(function () {
    $(this).addClass('active');
    $('#operationListNext').removeClass('active');
    $('#beginnerGuideNext').removeClass('active');
    $('#grammarNext').removeClass('active');
  });
  var heroes = db['sheet1'];
  var mun = getParams('param1');
  NameS = heroes[mun].分类名称;
  NameSnext = getParams('param2');
  $('#operNameSnext').text(NameSnext);
  var operNameNext =
    "<a class='button-item-A' href='enter_list.html?param3=true&param4=" +
    mun +
    "'>" +
    NameS +
    '</a>';
  $('#operNameS').append(operNameNext);
  LoadSuggestion(recordPath);
});
