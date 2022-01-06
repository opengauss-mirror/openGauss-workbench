export type dataItem = {
    label: string,
    value: string;
    color?: string;
}

export const baseInfoOption: Array<dataItem> = [
    { label: 'debug_query_id', value: 'debug_query_id' },
    { label: 'db_name', value: 'db_name' },
    { label: 'schema_name', value: 'schema_name' },
    { label: 'start_time', value: 'start_time' },
    { label: 'finish_time', value: 'finish_time' },
    { label: 'user_name', value: 'user_name' },
    { label: 'application_name', value: 'application_name' },
    { label: 'socket', value: 'socket' },
];

export const executeOption: Array<Array<dataItem>> = [
    [
        { label: 'n_returned_rows', value: 'n_returned_rows' },
        { label: 'n_soft_parse', value: 'n_soft_parse' },
    ],
    [
        { label: 'n_tuples_fetched', value: 'n_tuples_fetched' },
        { label: 'n_hard_parse', value: 'n_hard_parse' },
    ],
    [
        { label: 'n_tuples_returned', value: 'n_tuples_returned' },
        { label: 'net_send_info_size', value: 'net_send_info_size' },
    ],
    [
        { label: 'n_tuples_inserted', value: 'n_tuples_inserted' },
        { label: 'net_recv_info_size', value: 'net_recv_info_size' },
    ],
    [
        { label: 'n_tuples_updated', value: 'n_tuples_updated' },
        { label: 'net_stream_send_info_size', value: 'net_stream_send_info_size' },
    ],
    [
        { label: 'n_tuples_deleted', value: 'n_tuples_deleted' },
        { label: 'net_stream_recv_info_size', value: 'net_stream_recv_info_size' },
    ],
    [
        { label: 'blocks_hit_rate', value: 'blocks_hit_rate' },
        { label: 'net_send_info_calls', value: 'net_send_info_calls' },
    ],
    [
        { label: 'lock_count', value: 'lock_count' },
        { label: 'net_recv_info_calls', value: 'net_recv_info_calls' },
    ],
    [
        { label: 'lock_wait_count', value: 'lock_wait_count' },
        { label: 'net_stream_send_info_calls', value: 'net_stream_send_info_calls' },
    ],
    [
        { label: 'lock_max_count', value: 'lock_max_count' },
        { label: 'net_stream_recv_info_calls', value: 'net_stream_recv_info_calls' },
    ]
];

export const consumingOption: Array<dataItem> = [
    { label: 'execution_time', value: 'execution_time', color: '#37D4D1' },
    { label: 'net_send_info_time', value: 'net_send_info_time', color: '#2830FF' },
    { label: 'parse_time', value: 'parse_time', color: '#00C7F9' },
    { label: 'net_recv_info_time', value: 'net_recv_info_time', color: '#E64A19' },
    { label: 'plan_time', value: 'plan_time', color: '#0D86E2' },
    { label: 'net_stream_send_info_time', value: 'net_stream_send_info_time', color: '#0F866A' },
    { label: 'rewrite_time', value: 'rewrite_time', color: '#425ADD' },
    { label: 'net_stream_recv_info_time', value: 'net_stream_recv_info_time', color: '#FFAB94' },
    { label: 'pl_execution_time', value: 'pl_execution_time', color: '#8B00E1' },
    { label: 'lock_time', value: 'lock_time', color: '#5CDF73' },
    { label: 'pl_compilation_time', value: 'pl_compilation_time', color: '#9CCC65' },
    { label: 'lock_wait_time', value: 'lock_wait_time', color: '#FEEC21' },
    { label: 'data_io_time', value: 'data_io_time', color: '#A97526' },
];
