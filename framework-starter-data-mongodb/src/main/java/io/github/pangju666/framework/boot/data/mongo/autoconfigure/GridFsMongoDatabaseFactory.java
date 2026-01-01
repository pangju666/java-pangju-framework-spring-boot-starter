package io.github.pangju666.framework.boot.data.mongo.autoconfigure;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoProperties;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * <p>copy from org.springframework.boot.data.mongodb.autoconfigure.GridFsMongoDatabaseFactory</p>
 */
class GridFsMongoDatabaseFactory implements MongoDatabaseFactory {

	private final MongoDatabaseFactory mongoDatabaseFactory;

	private final DataMongoProperties properties;

	GridFsMongoDatabaseFactory(MongoDatabaseFactory mongoDatabaseFactory, DataMongoProperties properties) {
		Assert.notNull(mongoDatabaseFactory, "'mongoDatabaseFactory' must not be null");
		Assert.notNull(properties, "'properties' must not be null");
		this.mongoDatabaseFactory = mongoDatabaseFactory;
		this.properties = properties;
	}

	@Override
	public MongoDatabase getMongoDatabase() throws DataAccessException {
		String gridFsDatabase = getGridFsDatabase();
		if (StringUtils.hasText(gridFsDatabase)) {
			return this.mongoDatabaseFactory.getMongoDatabase(gridFsDatabase);
		}
		return this.mongoDatabaseFactory.getMongoDatabase();
	}

	@Override
	public MongoDatabase getMongoDatabase(String dbName) throws DataAccessException {
		return this.mongoDatabaseFactory.getMongoDatabase(dbName);
	}

	@Override
	public PersistenceExceptionTranslator getExceptionTranslator() {
		return this.mongoDatabaseFactory.getExceptionTranslator();
	}

	@Override
	public ClientSession getSession(ClientSessionOptions options) {
		return this.mongoDatabaseFactory.getSession(options);
	}

	@Override
	public MongoDatabaseFactory withSession(ClientSession session) {
		return this.mongoDatabaseFactory.withSession(session);
	}

	private @Nullable String getGridFsDatabase() {
		return this.properties.getGridfs().getDatabase();
	}

}
