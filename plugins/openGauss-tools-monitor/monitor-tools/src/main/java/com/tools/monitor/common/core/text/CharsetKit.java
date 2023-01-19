package com.tools.monitor.common.core.text;

import com.tools.monitor.util.StringUtils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * PageDomain
 *
 * @author liu
 * @since 2022-10-01
 */
public class CharsetKit {
    /**
     * ISO-8859-1
     */
    public static final String ISO_8859_1 = "ISO-8859-1";
    /**
     * UTF-8
     */
    public static final String UTF_8 = "UTF-8";
    /**
     * GBK
     */
    public static final String GBK = "GBK";

    /**
     * ISO-8859-1
     */
    public static final Charset CHARSET_ISO_8859_1 = Charset.forName(ISO_8859_1);
    /**
     * UTF-8
     */
    public static final Charset CHARSET_UTF_8 = Charset.forName(UTF_8);
    /**
     * GBK
     */
    public static final Charset CHARSET_GBK = Charset.forName(GBK);

    /**
     * Charset
     *
     * @param charset
     * @return Charset
     */
    public static Charset charset(String charset) {
        return StringUtils.isEmpty(charset) ? Charset.defaultCharset() : Charset.forName(charset);
    }

    /**
     * convert
     *
     * @param source  source
     * @param srcCharset   srcCharset
     * @param destCharset  destCharset
     * @return String
     */
    public static String convert(String source, String srcCharset, String destCharset) {
        return convert(source, Charset.forName(srcCharset), Charset.forName(destCharset));
    }

    /**
     * convert
     *
     * @param source source
     * @param srcCharset srcCharset
     * @param destCharset destCharset
     * @return String
     */
    public static String convert(String source, Charset srcCharset, Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = StandardCharsets.ISO_8859_1;
        }
        if (null == destCharset) {
            destCharset = StandardCharsets.UTF_8;
        }
        if (StringUtils.isEmpty(source) || srcCharset.equals(destCharset)) {
            return source;
        }
        return new String(source.getBytes(srcCharset), destCharset);
    }

    /**
     * systemCharset
     *
     * @return String
     */
    public static String systemCharset() {
        return Charset.defaultCharset().name();
    }
}
