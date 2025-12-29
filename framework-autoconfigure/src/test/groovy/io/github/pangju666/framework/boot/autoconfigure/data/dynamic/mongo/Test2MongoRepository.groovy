package io.github.pangju666.framework.boot.autoconfigure.data.dynamic.mongo

import io.github.pangju666.framework.boot.data.dynamic.mongo.DynamicMongo
import io.github.pangju666.framework.data.mongodb.repository.BaseMongoRepository
import org.springframework.stereotype.Repository

@DynamicMongo("test2")
@Repository
interface Test2MongoRepository extends BaseMongoRepository<io.github.pangju666.framework.boot.autoconfigure.data.mongo.TestDocument, String> {
}