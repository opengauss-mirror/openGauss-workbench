package com.nctigba.observability.instance.entity;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author auto
 * @since 2022-09-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Searc implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Index Name
     */
    private String indexName;

    /**
     * Log Type
     */
    private String type;

    /**
     * Filter Criteria
     */
    private String feilt;

    /**
     * start time
     */
    private String starDate;

    /**
     * End time
     */
    private String endDate;


}
