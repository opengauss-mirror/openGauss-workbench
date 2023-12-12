/*
 * Copyright (c) GBA-NCTI-ISDC. 2022-2023. All rights reserved.
 */

package com.nctigba.datastudio.model.dto;

import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;

/**
 * UpdateUserPasswordDTO
 *
 * @since 2023-6-26
 */
@NoArgsConstructor
@Data
@Generated
public class UpdateUserPasswordDTO {
    private String uuid;
    private String oldPassword;
    private String newPassword;
    private String userName;
    private String type;
    private String loginUserPassword;
}
