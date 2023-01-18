export type OptionType = {
    label: string;
    value: string
}

export type DataType = {
    objList: Array<OptionType>;
    baseInfoRowData: Record<string, string>
}

export const baseInfoOption: Array<OptionType> = [
    {
        label: 'schemaname',
        value: 'schemaname'
    },
    {
        label: 'relname',
        value: 'relname'
    },
    {
        label: 'objectType',
        value: 'object_type'
    },
    {
        label: 'objectSize',
        value: 'object_size'
    },
    {
        label: 'nLiveTup',
        value: 'n_live_tup'
    },
    {
        label: 'nDeadTup',
        value: 'n_dead_tup'
    },
    {
        label: 'deadTupRatio',
        value: 'dead_tup_ratio'
    },
    {
        label: 'lastVacuum',
        value: 'last_vacuum'
    },
    {
        label: 'lastVacuumAutovacuum',
        value: 'last_vacuum_autovacuum'
    },
    {
        label: 'lastAnalyze',
        value: 'last_analyze'
    },
    {
        label: 'lastAutoanalyze',
        value: 'last_autoanalyze'
    },
];

export type tableStructureType = {
    attlen: number;
    attname: string;
    attnotnull: boolean;
    attnum: number;
    description: string;
    typname: string;
}

export type tableIndexType = {
    relname: string;
    indisprimary: string;
    indisunique: string;
    indisclustered: string;
    indisvalid: string;
    indisreplident: string;
    def: string;
}

export type tableMetaDataType = {
    schemaname: string;
    relname: string;
    object_type: string;
    object_size: string;
    n_live_tup: string;
    n_dead_tup: string;
    dead_tup_ratio: string;
    last_vacuum: string;
    last_autovacuum: string;
    last_analyze: string;
    last_autoanalyze: string;
}
