package io.github.pangju666.framework.autoconfigure.data.mongo.factory;

import com.mongodb.ClientSessionOptions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.MongoDatabase;
import org.springframework.boot.autoconfigure.mongo.MongoConnectionDetails;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.support.PersistenceExceptionTranslator;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class GridFsMongoDatabaseFactory implements MongoDatabaseFactory {
	private final MongoDatabaseFactory mongoDatabaseFactory;
	private final MongoConnectionDetails connectionDetails;

	public GridFsMongoDatabaseFactory(MongoDatabaseFactory mongoDatabaseFactory,
									  MongoConnectionDetails connectionDetails) {
		Assert.notNull(mongoDatabaseFactory, "MongoDatabaseFactory must not be null");
		Assert.notNull(connectionDetails, "ConnectionDetails must not be null");
		this.mongoDatabaseFactory = mongoDatabaseFactory;
		this.connectionDetails = connectionDetails;
	}

	@Override
	public MongoDatabase getMongoDatabase() throws DataAccessException {
		String gridFsDatabase = getGridFsDatabase(this.connectionDetails);
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

	private String getGridFsDatabase(MongoConnectionDetails connectionDetails) {
		return (connectionDetails.getGridFs() != null) ? connectionDetails.getGridFs().getDatabase() : null;
	}
}
