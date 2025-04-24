package io.github.pangju666.framework.autoconfigure.web.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class WebLogDocument extends WebLog {
	@Id
	private String id;
}