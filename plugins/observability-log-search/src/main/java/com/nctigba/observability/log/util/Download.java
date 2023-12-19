/*
 *  Copyright (c) GBA-NCTI-ISDC. 2022-2024.
 *
 *  openGauss DataKit is licensed under Mulan PSL v2.
 *  You can use this software according to the terms and conditions of the Mulan PSL v2.
 *  You may obtain a copy of Mulan PSL v2 at:
 *
 *  http://license.coscl.org.cn/MulanPSL2
 *
 *  THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 *  EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 *  MERCHANTABILITY OR FITFOR A PARTICULAR PURPOSE.
 *  See the Mulan PSL v2 for more details.
 *  -------------------------------------------------------------------------
 *
 *  Download.java
 *
 *  IDENTIFICATION
 *  plugins/observability-log-search/src/main/java/com/nctigba/observability/log/util/Download.java
 *
 *  -------------------------------------------------------------------------
 */

package com.nctigba.observability.log.util;

import java.io.File;
import java.net.URL;

import cn.hutool.core.io.FileUtil;

public class Download {
	public static File download(String fileUrl, String path) {
		var file = new File(path);
		try {
			var parent = file.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
			var url = new URL(fileUrl);
			var conn = url.openConnection();
			conn.setConnectTimeout(3 * 1000);
			var is = conn.getInputStream();
			FileUtil.writeFromStream(is, file);
			file = file.getCanonicalFile();
		} catch (Exception e) {
			throw new RuntimeException("download fail", e);
		}
		return file;
	}
}