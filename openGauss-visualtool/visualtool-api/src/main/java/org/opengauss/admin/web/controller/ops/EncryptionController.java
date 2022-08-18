package org.opengauss.admin.web.controller.ops;

import org.opengauss.admin.common.core.domain.AjaxResult;
import org.opengauss.admin.system.service.ops.impl.EncryptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lhf
 * @date 2022/11/29 22:38
 **/
@RestController
@RequestMapping("/encryption")
public class EncryptionController {

    @Autowired
    private EncryptionUtils encryptionUtils;

    @GetMapping("/getKey")
    public AjaxResult getKey() {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("key", encryptionUtils.getKey());
        return ajax;
    }
}
