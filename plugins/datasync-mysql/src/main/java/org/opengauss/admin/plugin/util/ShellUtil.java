/**
 Copyright Looly.

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
package org.opengauss.admin.plugin.util;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.StaticLog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * @className: ShellUtil
 * @description: ShellUtil
 * @author: xielibo
 * @date: 2022-10-27 13:38
 **/
public class ShellUtil {

    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static String execForStr(String... cmds) throws IORuntimeException {
        return execForStr(CharsetUtil.systemCharset(), cmds);
    }
    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static String execForStr(File dir, String... cmds) throws IORuntimeException {
        return getResult(exec(dir,cmds), CharsetUtil.systemCharset());
    }

    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static String execForStr(Charset charset, String... cmds) throws IORuntimeException {
        return getResult(exec(cmds), charset);
    }
    public static String execForErrStr(Charset charset,String cmd) throws IORuntimeException {
        return getResultErr(exec(cmd), charset);
    }


    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static String execForStr(Charset charset, String cmds) throws IORuntimeException {
        return getResult(exec(cmds), charset);
    }

    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static List<String> execForLines(String... cmds) throws IORuntimeException {
        return execForLines(CharsetUtil.systemCharset(), cmds);
    }

    /**
     * Execute system commands, using the system default encoding
     *
     */
    public static List<String> execForLines(Charset charset, String... cmds) throws IORuntimeException {
        return getResultLines(exec(cmds), charset);
    }

    /**
     * exec
     * When the command has parameters, the parameter can be used as one of the parameters, or the command and parameters can be combined into a string to pass in
     *
     */
    public static Process exec(File directory, String... cmds) {
        Process process;
        try {
            process = new ProcessBuilder(handleCmds(cmds)).directory(directory).redirectErrorStream(true).start();
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return process;
    }

    /**
     * exec
     * When the command has parameters, the parameter can be used as one of the parameters, or the command and parameters can be combined into a string to pass in
     *
     */
    public static Process exec(String cmd) {
        Process process;
        try {
            Runtime runtime = Runtime.getRuntime();
            if(cmd.indexOf("|")>0){
                String[] cmdArr = {"sh","-c",cmd};
                process = runtime.exec(cmdArr);
            }else{
                process = runtime.exec(cmd);
            }
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
        return process;
    }

    /**
     * exec
     * When the command has parameters, the parameter can be used as one of the parameters, or the command and parameters can be combined into a string to pass in
     *
     */
    public static Process exec(String[] envp, String... cmds) {
        return exec(envp, null, cmds);
    }

    /**
     * exec
     * When the command has parameters, the parameter can be used as one of the parameters, or the command and parameters can be combined into a string to pass in
     *
     */
    public static Process exec(String[] envp, File dir, String... cmds) {
        try {
            return Runtime.getRuntime().exec(handleCmds(cmds), envp, dir);
        } catch (IOException e) {
            throw new IORuntimeException(e);
        }
    }


    /**
     * Obtain the command execution result, use the system default encoding, and destroy the process after acquisition
     *
     */
    public static List<String> getResultLines(Process process) {
        return getResultLines(process, CharsetUtil.systemCharset());
    }

    /**
     * Obtain the command execution result, use the system default encoding, and destroy the process after acquisition
     *
     */
    public static List<String> getResultLines(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getInputStream();
            return IoUtil.readLines(in, charset, new ArrayList<>());
        } finally {
            IoUtil.close(in);
            destroy(process);
        }
    }

    /**
     * Obtain the command execution result, use the system default encoding, and destroy the process after acquisition
     *
     */
    public static String getResult(Process process) {
        return getResult(process, CharsetUtil.systemCharset());
    }

    /**
     * Obtain the command execution result and destroy the process after obtaining it
     *
     */
    public static String getResult(Process process, Charset charset) {
        InputStream in = null;
        InputStream errorStream = null;
        try {
            in = process.getInputStream();
            errorStream = process.getErrorStream();
            String errorResult = IoUtil.read(errorStream, charset);
            if(StrUtil.isNotBlank(errorResult)){
                StaticLog.warn("Shell command execution error, because {}",errorResult);
            }
            return IoUtil.read(in, charset);
        } finally {
            IoUtil.close(in);
            IoUtil.close(errorStream);
            destroy(process);
        }
    }

    /**
     * Get the wrong execution result, destroy the process after getting it
     *
     */
    public static String getResultErr(Process process, Charset charset) {
        InputStream in = null;
        InputStream errorStream = null;
        try {
            in = process.getInputStream();

            errorStream = process.getErrorStream();
            return IoUtil.read(errorStream, charset);
        } finally {
            IoUtil.close(in);
            IoUtil.close(errorStream);
            destroy(process);
        }
    }


    /**
     * Obtain the abnormal result of command execution, use the system default encoding, and destroy the process after acquisition
     *
     */
    public static String getErrorResult(Process process) {
        return getErrorResult(process, CharsetUtil.systemCharset());
    }

    /**
     * Obtain the abnormal result of command execution, and destroy the process after acquisition
     *
     */
    public static String getErrorResult(Process process, Charset charset) {
        InputStream in = null;
        try {
            in = process.getErrorStream();
            return IoUtil.read(in, charset);
        } finally {
            IoUtil.close(in);
            destroy(process);
        }
    }

    /**
     * destroy
     *
     */
    public static void destroy(Process process) {
        if (null != process) {
            process.destroy();
        }
    }

    /**
     * Add a hook after the JVM is closed to perform certain operations when the JVM is closed
     *
     */
    public static void addShutdownHook(Runnable hook) {
        Runtime.getRuntime().addShutdownHook((hook instanceof Thread) ? (Thread) hook : new Thread(hook));
    }

    /**
     * Get the number of processors available to the JVM (usually the number of CPU cores)
     *
     */
    public static int getProcessorCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Get the remaining amount of memory in the JVM, unit byte
     *
     */
    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    /**
     * Obtain the total amount of memory that the JVM has obtained from the system, in byte
     *
     */
    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    /**
     * Obtain the maximum amount of memory that can be obtained from the system in the JVM, in bytes, subject to the -Xmx parameter
     *
     */
    public static long getMaxMemory() {
        return Runtime.getRuntime().maxMemory();
    }

    /**
     * Obtain the maximum available memory of the JVM, the calculation method is:
     * Maximum memory - total memory + remaining memory
     *
     */
    public static long getUsableMemory() {
        return getMaxMemory() - getTotalMemory() + getFreeMemory();
    }

    /**
     * To get the current process ID, first get the process name, read the ID value before @, if it does not exist, read the hash value of the process name
     *
     */
    public static int getPid() throws UtilException {
        final String processName = ManagementFactory.getRuntimeMXBean().getName();
        if (StrUtil.isBlank(processName)) {
            throw new UtilException("Process name is blank!");
        }
        final int atIndex = processName.indexOf('@');
        if (atIndex > 0) {
            return Integer.parseInt(processName.substring(0, atIndex));
        } else {
            return processName.hashCode();
        }
    }

    /**
     * Processing commands, multi-line commands are returned as they are, and single-line commands are split and processed
     *
     */
    private static String[] handleCmds(String... cmds) {
        if (ArrayUtil.isEmpty(cmds)) {
            throw new NullPointerException("Command is empty !");
        }

        if (1 == cmds.length) {
            final String cmd = cmds[0];
            if (StrUtil.isBlank(cmd)) {
                throw new NullPointerException("Command is blank !");
            }
            cmds = cmdSplit(cmd);
        }
        return cmds;
    }

    /**
     * Command splitting, use spaces to split, consider the case of double quotes and single quotes
     *
     */
    private static String[] cmdSplit(String cmd) {
        final List<String> cmds = new ArrayList<>();

        final int length = cmd.length();
        final Stack<Character> stack = new Stack<>();
        boolean inWrap = false;
        final StrBuilder cache = StrUtil.strBuilder();

        char c;
        for (int i = 0; i < length; i++) {
            c = cmd.charAt(i);
            switch (c) {
                case CharUtil.SINGLE_QUOTE:
                case CharUtil.DOUBLE_QUOTES:
                    if (inWrap) {
                        if (c == stack.peek()) {
                            stack.pop();
                            inWrap = false;
                        }
                        cache.append(c);
                    } else {
                        stack.push(c);
                        cache.append(c);
                        inWrap = true;
                    }
                    break;
                case CharUtil.SPACE:
                    if (inWrap) {
                        cache.append(c);
                    } else {
                        cmds.add(cache.toString());
                        cache.reset();
                    }
                    break;
                default:
                    cache.append(c);
                    break;
            }
        }

        if (cache.hasContent()) {
            cmds.add(cache.toString());
        }

        return cmds.toArray(new String[0]);
    }


}
