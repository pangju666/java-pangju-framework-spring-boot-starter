package io.github.pangju666.framework.boot.autoconfigure.data.mongo

import io.github.pangju666.framework.data.mongodb.repository.BaseMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface TestMongoRepository extends BaseMongoRepository<TestDocument, String> {
}