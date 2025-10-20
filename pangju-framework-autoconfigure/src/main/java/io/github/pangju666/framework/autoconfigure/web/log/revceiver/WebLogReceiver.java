package io.github.pangju666.framework.autoconfigure.web.log.revceiver;

import io.github.pangju666.framework.autoconfigure.web.log.WebLog;

public interface WebLogReceiver {
	void receive(WebLog webLog);
}
