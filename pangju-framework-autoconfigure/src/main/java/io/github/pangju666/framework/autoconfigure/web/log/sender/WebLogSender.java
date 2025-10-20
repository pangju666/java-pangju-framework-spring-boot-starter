package io.github.pangju666.framework.autoconfigure.web.log.sender;

import io.github.pangju666.framework.autoconfigure.web.log.WebLog;

public interface WebLogSender {
	void send(WebLog webLog);
}
