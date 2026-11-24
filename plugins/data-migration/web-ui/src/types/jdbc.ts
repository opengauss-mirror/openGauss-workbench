/*
 * Copyright (c) 2026 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

export enum JDBCType {
  MySQL="MYSQL",
  openGauss="OPENGAUSS",
  PostgreSQL="POSTGRESQL",
  Elasticsearch="ELASTICSEARCH",
  Milvus="MILVUS"
}

export interface JdbcConfig {
  defaultPort: number;
  urlPattern?: string;
}

export namespace JDBCType {
  const NORMALIZE_MAPPING: Record<string, string> = {
    'MYSQL': 'MySQL',
    'OPENGAUSS': 'openGauss',
    'POSTGRESQL': 'PostgreSQL',
    'ELASTICSEARCH': 'Elasticsearch',
    'MILVUS': 'Milvus'
  };

  const CONFIG_MAPPING: Record<JDBCType, JdbcConfig> = {
    [JDBCType.MySQL]: {
      defaultPort: 3306,
      urlPattern: 'jdbc:mysql://{host}:{port}/{defaultDb}',
    },
    [JDBCType.openGauss]: {
      defaultPort: 5432,
      urlPattern: 'jdbc:opengauss://{host}:{port}/{defaultDb}',
    },
    [JDBCType.PostgreSQL]: {
      defaultPort: 5432,
      urlPattern: 'jdbc:postgresql://{host}:{port}/{defaultDb}',
    },
    [JDBCType.Elasticsearch]: {
      defaultPort: 9200,
    },
    [JDBCType.Milvus]: {
      defaultPort: 19530,
    }
  };

  const DEFAULT_CONFIG: JdbcConfig = {
    defaultPort: 19530,
  };

  export function normalize(sqlType: string): string {
    return NORMALIZE_MAPPING[sqlType.toUpperCase()] || sqlType;
  }

  export function getConfig(type: JDBCType): JdbcConfig {
    return CONFIG_MAPPING[type] || DEFAULT_CONFIG;
  }

  export function getDefaultPort(type: JDBCType): number {
    return getConfig(type).defaultPort;
  }

  export function getAllTypes(): JDBCType[] {
    return Object.values(JDBCType) as JDBCType[];
  }
}
