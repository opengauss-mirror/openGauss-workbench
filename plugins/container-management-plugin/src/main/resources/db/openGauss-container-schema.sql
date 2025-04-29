CREATE TABLE IF NOT EXISTS k8s_cluster
(
    id             varchar(60),
    name         varchar(30),
    api_server     varchar(100),
    port           varchar(11),
    token          varchar(3000),
    domain       varchar(30),
    prometheus_url varchar(100),
    harbor_address varchar(100),
    resource_pool  varchar(255),
    is_enable      int2,
    create_time    timestamp,
    update_time    timestamp,
    CONSTRAINT k8s_cluster_pkey PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS open_gauss_cluster
(
    id              int8 AUTOINCREMENT,
    k8s_id          varchar(60),
    name          varchar(100),
    namespace       varchar(50),
    port            int4,
    version       varchar(50),
    "image"           varchar(100),
    cmdb_id         varchar(60),
    app_code        varchar(20),
    cost_center     varchar(100),
    app_name        varchar(50),
    create_user     varchar(100),
    cpu_request     varchar(10),
    mem_request     varchar(10),
    cpu_limit       int4,
    disk_capacity   int4,
    resource_pool   varchar(50),
    remark          varchar(200),
    create_time     timestamp,
    update_time     timestamp,
    is_test_cluster int2,
    is_add_monitor  int2,
    is_add_cmdb     int2,
    is_add4a        int2,
    arch_type       varchar(60),
    architecture    varchar(60),
    backup_image    varchar(50),
    exporter_image  varchar(50),
    cleanup_image   varchar(50),
    CONSTRAINT pk_opengauss_cluster_primary_id PRIMARY KEY (id)
);


CREATE TABLE IF NOT EXISTS open_gauss_image
(
    id           int8 AUTOINCREMENT,
    type       varchar(20),
    architecture varchar(20),
    os           varchar(20),
    version    varchar(20),
    "image"        varchar(50),
    priority     int4,
    describe  varchar(50),
    create_time  timestamp,
    update_time  timestamp,
    enable     int2,
    CONSTRAINT open_gauss_image_primary_id PRIMARY KEY (id)
    );
CREATE UNIQUE INDEX IF NOT exists open_gauss_image_name_idx ON opengauss_api.open_gauss_image (image);

CREATE TABLE IF NOT EXISTS open_gauss_operator
(
    id                      int8 AUTOINCREMENT,
    k8s_cluster_id          varchar(60),
    name                    varchar(100),
    type                    varchar(10),
    create_time             timestamp,
    update_time             timestamp,
    current_manage_quantity int4,
    max_manage_quantity     int4,
    k8s_cluster_name        varchar(100),
    CONSTRAINT pk_opengauss_operator_primary_id PRIMARY KEY (id)
);