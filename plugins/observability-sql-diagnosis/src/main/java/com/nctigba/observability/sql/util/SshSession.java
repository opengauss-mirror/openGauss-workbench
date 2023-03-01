package com.nctigba.observability.sql.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;

import org.opengauss.admin.common.exception.ops.OpsException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SshSession implements AutoCloseable {
	private static final int SESSION_TIMEOUT = 10000;
	private static final int CHANNEL_TIMEOUT = 50000;

	public enum command {
		ARCH("arch"),
		CD("cd {0}"),
		LS("ls {0}"),
		STAT("stat {0}"),
		WGET("wget {0}"),
		TAR("tar zxf {0}"),
		UNZIP("unzip {0}"),
		APPEND_FILE(""),
		CHECK_USER("cat /etc/passwd | awk -F \":\" \"'{print $1}\"|grep {0} | wc -l"),
		CREATE_USER("useradd omm && echo ''{0} ALL=(ALL) ALL'' >> /etc/sudoers"),
		CHANGE_PASSWORD("passwd {1}"),
		PS("ps -ef|grep {0} |grep -v grep |awk ''{print $2}''"),
		KILL("kill -9 {0}");

		private String cmd;

		command(String cmd) {
			this.cmd = cmd;
		}

		public String parse(Object... args) {
			return MessageFormat.format(cmd, args);
		}
	}

	public boolean test(String command) throws IOException {
		try {
			execute(command);
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	public String execute(command command) throws IOException {
		return execute(command.cmd, null, true);
	}

	public String execute(command command, Map<String, String> autoResponse) throws IOException {
		return execute(command.cmd, autoResponse, true);
	}

	public String execute(String command) throws IOException {
		return execute(command, null, true);
	}

	public String executeNoWait(String command) throws IOException {
		return execute(command, null, false);
	}

	public String execute(String command, Map<String, String> autoResponse, boolean wait) throws IOException {
		log.info("Execute an order：{}", command);
		ChannelExec channelExec;
		try {
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setPtyType("dump");
			channelExec.setPty(true);
		} catch (JSchException e) {
			throw new OpsException("Obtaining the exec channel fails");
		}
		channelExec.setCommand(command);
		try {
			channelExec.connect(CHANNEL_TIMEOUT);
		} catch (JSchException e) {
			throw new OpsException("Command execution exception");
		}
		StringBuilder resultStrBuilder = new StringBuilder();
		InputStream in = channelExec.getInputStream();
		OutputStream out = channelExec.getOutputStream();
		byte[] tmp = new byte[1024];
		while (true) {
			while (in.available() > 0) {
				int i = in.read(tmp, 0, 1024);
				if (i < 0) {
					break;
				}
				String msg = new String(tmp, 0, i);
				resultStrBuilder.append(msg);
			}
			if (!wait && autoResponse == null && resultStrBuilder.length() > 0)
				return resultStrBuilder.toString().trim();
			if (channelExec.isClosed()) {
				if (in.available() > 0) {
					continue;
				}
				in.close();
				out.close();
				int exitStatus = channelExec.getExitStatus();
				if (exitStatus != 0)
					throw new RuntimeException(resultStrBuilder.toString().trim());
				return resultStrBuilder.toString().trim();
			}
			ThreadUtil.sleep(2000);
			if (autoResponse != null) {
				autoResponse.forEach((k, v) -> {
					if (resultStrBuilder.toString().trim().endsWith(k.trim())) {
						try {
							out.write((v.trim() + "\r").getBytes(StandardCharsets.UTF_8));
							out.flush();
							resultStrBuilder.append(v.trim() + "\r");
						} catch (IOException e) {
						}
					}
				});
			}
		}
	}

	public synchronized void upload(String source, String target) {
		try {
			ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");
			channel.connect();
			channel.put(source, target, ChannelSftp.RESUME);
		} catch (Exception e) {
			log.error("upload fail", e);
			throw new RuntimeException(e);
		}
	}

	private Session session;

	private SshSession(String host, Integer port, String username, String password) throws IOException {
		JSch jSch = new JSch();
		try {
			session = jSch.getSession(username, host, port);
		} catch (JSchException e) {
			throw new OpsException("Connection establishment fail");
		}
		session.setPassword(password);
		session.setConfig("StrictHostKeyChecking", "no");
		try {
			session.connect(SESSION_TIMEOUT);
		} catch (JSchException e) {
			throw new OpsException(host + "Connection establishment fail");
		}
	}

	public static SshSession connect(String host, Integer port, String username, String password) throws IOException {
		return new SshSession(host, port, username, password);
	}

	@Override
	public void close() {
		session.disconnect();
	}
}