package io.github.pangju666.framework.autoconfigure.web.sender;

import io.github.pangju666.framework.autoconfigure.web.model.WebLog;

public interface WebLogSender {
	void send(WebLog webLog);
}
