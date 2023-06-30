export default {
    app: {
        name: 'WorkBench',
        menuName: 'Monitor',
        refresh: 'refresh',
        autoRefresh: 'refresh',
        custom: 'Custom',
        startDate: 'Start Date',
        endDate: 'End Date',
        to: 'To',
        running: 'running',
        disconnect: 'disconnect',
        unknown: 'unknown',
        query: 'query',
        delete: 'delete',
        cancel: 'cancel',
        confirm: 'save',
        add: 'add',
        edit: 'edit',
        operate: 'operate',
        view: 'view',
        download: 'download',
        reset: 'Reset',
        back: 'Back',
        preview: 'Preview',
        gotoInstall: 'go to install',
        queryFail: 'Query failed',
        saveSuccess: 'Save successful',
        saveFail: 'Save failed',
        testSuccess: 'Test successful',
        testFail: 'Test failed',
        delSuccess: 'Deletion successful',
        delFail: 'Deletion failed',
        selecteDataTip: 'Please select a piece of data',
        selectDatasTip: 'Please select the datas',
        tip: 'Tip',
        inputTip: 'Please input ',
        setAlertConfig: 'Set up the Alert interface',
        selectUpdateDataTip: 'Please select the data first before editing',
        deleteTip: 'Are you sure you want to delete this data?',
        keepOneData: 'Please keep at least one piece of data',
        uninstallTitleTip: 'Please install the prometheus server',
        uninstallTip: 'Before using the Alert Center module and the Notify Configuration module, you need to install Prometheus. Please go to "Intelligent OPS - Instance Monitoring " to install the server Prometheus. Otherwise, it will not be available.',
        disConfigTip: 'Before using the Alert Center module and the Notify Configuration module, set up the alert interface first.',
    },
    alertRule: {
        title: 'alert rule',
        alertTitle: 'alert rule',
        notifyTitle: 'notify rule',
        addTitle: 'add rule',
        detailTitle: 'rule detail',
        editTitle: 'edit rule',
        ruleName: 'ruleName',
        ruleType: 'ruleType',
        level: 'level',
        index: 'index',
        ruleItem: 'alertRule',
        ruleExpComb: 'ruleCombination',
        ruleContent: 'content',
        notifyDuration: 'statisticalDuration',
        alertDesc: 'description',
        repeat: 'duplicate',
        silence: 'silence',
        silenceTime: 'silenceTime',
        alertNotify: 'alertNotify',
        notifyWay: 'notifyWay',
        second: 'second',
        minute: 'minute',
        hour: 'hour',
        day: 'day',
        log: 'log',
        serious: 'serious',
        warn: 'warn',
        info: 'info',
        ruleNamePlaceholder: 'select the rule name please',
        selectRuleType: 'select the rule type please',
        selectLevelType: 'select the level please',
        isRepeat: 'Yes',
        isNotRepeat: 'No',
        isSilence: 'Yes',
        isNotSilence: 'No',
        to: 'to',
        firing: 'firing',
        recover: 'recover',
        and: 'and',
        or: 'or',
        table: ['ruleName', 'ruleType', 'level', 'isRepeat', 'isSilence', 'alertNotify', 'alertContent', 'operate', 'alertRule', 'ruleCombination'],
        ruleItemNum: 'number',
        ruleItemExp: 'ruleExpression',
        normalAction: 'keeping happen',
        increaseAction: 'keeping increase',
        decreaseAction: 'keeping decrease',
        alertContentTitle: 'Alert content description',
        alertContentTip: 'The main purpose of the alert content is to notify the user, and it supports configuring parameters using ${xxx} to represent the parameters. For example, the alert content is "In the last minute, the CPU usage rate of ${nodeName} has been greater than 90%", where ${nodeName} is a parameter. The configurable parameters are as follows:'
    },
    alertTemplate: {
        title: 'alert template',
        addTitle: 'add template',
        editTitle: 'edit template',
        detailTitle: 'template detail',
        templateName: 'template name',
        selectRule: 'select the rule',
        templateNamePlaceholder: 'Please enter template name',
        table: ['template name', 'operate'],
    },
    alertRecord: {
        title: 'alert record',
        alertTotal: 'total of alert',
        alerting: 'firing',
        alerted: 'recover',
        unread: 'unread',
        read: 'read',
        markAsRead: 'mark as read',
        markAsUnread: 'mark as unread',
        recordStatus: 'record status',
        alertStatus: 'alert status',
        level: 'alert level',
        recordStatusTip: 'Alert handling statistics',
        alertStatusTip: 'Alert status statistics',
        levelTip: 'Alert level statistics',
        cluster: 'cluster/instance',
        alertTimeRange: 'alert time range',
        startTimePlaceholder: 'start time',
        endTimePlaceholder: 'end time',
        table: ['instance', 'IP', 'datasoure type', 'alert template', 'alert rule', 'rule type', 'alert level', 'start time', 'end time', 'duration', 'alert status', 'notify way', 'record status', 'operate'],
        tableDataSelectTip: 'Please select at least one data',
        detailTitle: 'alert detail',
        alertInstance: 'alert instance',
        alertRule: 'alert rule',
        alertContent: 'alert content',
        alertRelationView: 'related view',
        clusterName: 'cluster name',
        IPAndPort: 'IP and port',
        nodeRole: 'cluster node role',
    },
    AlertClusterNodeConf: {
        title: 'alert configuration',
        confBtn: 'configuration',
        table: ['cluster/instance', 'datasoure type', 'IP', 'alert template', 'operate'],
        detailTitle: 'alert template config',
        selectedInstance: 'selected instances',
        alertTemplateTab: 'select alert template',
        alertRuleTab: 'select alert rule',
        generateTip1: 'The system will generate a default template name for the selected rules:"',
        generateTip2: '".You can also customize and modify the name.',
        setInterface: 'set alert interface',
        alertIp: 'Interface IP',
        alertPort: 'Interface Port',
        interfaceAddr: 'Full interface',
        nodeNamePlaceholder: 'Please input cluster/instance name'
    },
    notifyTemplate: {
        title: 'notify template',
        addTitle: 'add template',
        editTitle: 'edit template',
        detailTitle: 'template detail',
        templateNamePlaceholder: 'Please enter the name of the notify template',
        templateTypePlaceholder: 'Please enter the type of the notify template',
        templateDescPlaceholder: 'Please enter the description of the notify template',
        notifyTitlePlaceholder: 'Please enter the title',
        notifyContentPlaceholder: 'Please enter the notify content',
        templateName: 'templateName',
        templateType: 'templateType',
        notifyTitle: 'notifyTitle',
        notifyContent: 'notifyContent',
        templateDesc: 'description',
        email: 'email',
        WeCom: 'WeCom',
        DingTalk: 'DingTalk',
        previewContent: 'preview',
        notifyContentTitle: 'notify content description',
        notifyContentTip: 'The message content mainly wraps the alert content, adds additional content, and finally sends the wrapped alert content to the user. It supports configuring parameters using ${xxx} to represent the parameters. The configurable parameter meanings are as follows:'
    },
    notifyWay: {
        title: 'notify way',
        addTitle: 'add notify way',
        editTitle: 'edit notify way',
        detailTile: 'notify way detail',
        namePlaceholder: 'Please enter the name of the notify way',
        name: 'name',
        notifyType: 'notifyType',
        notifyTarget: 'notifyTarget',
        personId: 'person ID',
        deptId: 'department ID',
        emailPlaceholder: 'Please enter email address(es). If there are multiple email addresses, please separate them with commas',
        personIdPlaceholder: 'Please enter person ID(s). If there are multiple person, please separate them with commas. Either person ID or department ID is required (or both).',
        deptIdPlaceholder: 'Please enter department ID(s). If there are multiple department, please separate them with commas. Either person ID or department ID is required (or both).',
        templatePlaceholder: 'Please select a notify template',
        emailTemplate: 'emailTemplate',
        WeComTemplate: 'WeComTemplate',
        DingTalkTemplate: 'DingTalkTemplate',
        existOneOfTargetTip: 'Either person ID or department ID is required (or both)',
    },
    notifyConfig: {
        title: 'notify config',
        emailConfig: 'eamil config',
        weComConfig: 'WeCom config',
        dingTalkConfig: 'DingTalk config',
        senderEmail: 'email',
        sender: 'sender',
        sever: 'server',
        port: 'SMTP port',
        account: 'SMTP account',
        passwd: 'SMTP password',
        agentId: 'agent ID',
        weComAppKey: 'WeCom appKey',
        weComSecret: 'WeCom secret',
        appKey: 'appKey',
        dingSecret: 'app secret',
        inputTip: 'Pleasse input',
        test: 'test',
    }
}
