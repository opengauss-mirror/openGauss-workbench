package org.opengauss.admin.common.utils.excel;

import org.opengauss.admin.common.core.domain.model.ops.ImportAsynInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Progress Information Utility Class.
 *
 * @author zzh
 * @version 1.0
 * @data 2024/6/25 10:35
 */
public class ImportAsynInfoUtils {
    /**
     * Map for storing progress information.
     */
    public static Map<String, ImportAsynInfo> allAsynInfo = new HashMap<String, ImportAsynInfo>();

    /**
     * Create a UUID and store the progress information object.
     *
     * @param asynInfo
     * @return
     */
    public static String createUUID(ImportAsynInfo asynInfo) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        allAsynInfo.put(uuid, asynInfo);
        return uuid;
    }

    /**
     * Retrieve progress information by UUID.
     *
     * @param uuid
     * @return
     */
    public static ImportAsynInfo getAsynInfo(String uuid) {
        return allAsynInfo.get(uuid);
    }

    /**
     * Delete the progress information corresponding to the UUID.
     *
     * @param uuid
     * @return
     */
    public static void deleteAsynInfo(String uuid) {
        allAsynInfo.remove(uuid);
    }

    /**
     * Increment the number of processed data entries by one for the progress associated with the UUID.
     *
     * @param uuid
     */
    public static synchronized void addDoneSum(String uuid) {
        ImportAsynInfo asynInfo = getAsynInfo(uuid);
        asynInfo.setDoneSum(asynInfo.getDoneSum() + 1);
    }

    /**
     * Increment the number of failed data entries by one for the progress associated with the UUID.
     *
     * @param uuid
     */
    public static synchronized void addErrorSum(String uuid) {
        ImportAsynInfo asynInfo = getAsynInfo(uuid);
        asynInfo.setErrorSum(asynInfo.getErrorSum() + 1);
    }

    /**
     * Increment the number of successful data entries by one for the progress associated with the UUID.
     *
     * @param uuid
     */
    public static synchronized void addSuccessSum(String uuid) {
        ImportAsynInfo asynInfo = getAsynInfo(uuid);
        asynInfo.setSuccessSum(asynInfo.getSuccessSum() + 1);
    }
}
