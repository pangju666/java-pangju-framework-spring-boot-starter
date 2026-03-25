package io.github.pangju666.framework.boot.data.mongo.dynamic

import io.github.pangju666.framework.boot.data.mongo.DynamicMongo
import io.github.pangju666.framework.data.mongodb.repository.BaseMongoRepository
import org.springframework.stereotype.Repository

@DynamicMongo("test2")
@Repository
interface Test2MongoRepository extends BaseMongoRepository<TestDocument, String> {
}