package io.github.pangju666.framework.boot.autoconfigure.data.mongo

import io.github.pangju666.framework.data.mongodb.model.document.BaseDocument
import org.springframework.data.mongodb.core.mapping.Document

@Document("test")
class TestDocument extends BaseDocument {
}
