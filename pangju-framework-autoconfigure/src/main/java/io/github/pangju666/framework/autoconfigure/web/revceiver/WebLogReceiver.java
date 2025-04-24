package io.github.pangju666.framework.autoconfigure.web.revceiver;

import io.github.pangju666.framework.autoconfigure.web.model.WebLog;

public interface WebLogReceiver {
	void receive(WebLog webLog);
}
