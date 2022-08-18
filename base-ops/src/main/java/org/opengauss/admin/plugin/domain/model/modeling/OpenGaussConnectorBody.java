/**
 Copyright  (c) 2020 Huawei Technologies Co.,Ltd.
 Copyright  (c) 2021 openGauss Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package org.opengauss.admin.plugin.domain.model.modeling;

import org.opengauss.admin.common.core.domain.model.ops.OpsClusterNodeVO;
import lombok.Data;

/**
 *
 * @author LZW
 */
@Data
public class OpenGaussConnectorBody {

    private String id;

    private String ip;

    private Integer port;

    private String database;

    private String dbUser;

    private String dbPassword;

    private String schema;

    private static final long serialVersionUID = 1L;

    public OpenGaussConnectorBody(OpsClusterNodeVO clusterNodeVO) {
        this.setIp(clusterNodeVO.getPublicIp());
        this.setPort(clusterNodeVO.getDbPort());
        this.setDatabase(clusterNodeVO.getDbName());
        this.setDbUser(clusterNodeVO.getDbUser());
        this.setDbPassword(clusterNodeVO.getDbUserPassword());

    }
}
