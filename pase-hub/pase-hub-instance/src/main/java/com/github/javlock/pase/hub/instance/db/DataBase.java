package com.github.javlock.pase.hub.instance.db;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.hub.instance.config.db.DataBaseConfig;
import com.github.javlock.pase.web.crawler.data.UrlData;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.TableUtils;

public class DataBase {
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");
	private PaseHub hub;

	private JdbcPooledConnectionSource connectionSource;
	private Dao<UrlData, Integer> urlDAO;

	public DataBase(PaseHub instanceHub) {
		hub = instanceHub;
	}

	void createDAOs() throws SQLException {
		urlDAO = DaoManager.createDao(connectionSource, UrlData.class);// urls
	}

	void createSource() throws SQLException {
		DataBaseConfig dbconfig = hub.getConfig().getDbConfig();

		String databaseUrl = "jdbc:postgresql://" + dbconfig.getHost() + ":" + dbconfig.getPort() + "/"
				+ dbconfig.getDataBaseName() + "?user=" + dbconfig.getUser() + "&password=" + dbconfig.getPassword()
				+ "&ssl=" + dbconfig.isSsl();
		connectionSource = new JdbcPooledConnectionSource(databaseUrl);
		connectionSource.setMaxConnectionsFree(60);
		connectionSource.initialize();
	}

	private void createTableFor(Dao<?, ?> dao, Class<?> class1) throws SQLException {
		if (!dao.isTableExists()) {
			try {
				dao.executeRawNoArgs("DROP SEQUENCE urls_id_seq;");
			} catch (Exception e) { // IGNORE
			}
			TableUtils.createTableIfNotExists(connectionSource, class1);
		}
	}

	void createTables() throws SQLException {
		createTableFor(urlDAO, UrlData.class);
	}

	public void init() throws SQLException {
		createSource();
		createDAOs();
		createTables();

		readSettingsFromDb();
	}

	private void readSettingsFromDb() {
		/*
		 * for (RegExData regExData : regExDao) { storage.updateFilter(regExData);
		 * System.err.println(regExData); }
		 */
	}

	public void saveUrlData(UrlData urldata) throws SQLException {
		if (urlDAO.idExists(urldata.getHashId())) {
			UrlData fromDb = urlDAO.queryForId(urldata.getHashId());

			if (fromDb.getPageType() != urldata.getPageType()) {
				fromDb.setPageType(urldata.getPageType());
			}

			if (
			// null
			(fromDb.getTitle() == null && urldata.getTitle() != null)
					// no null but !equals
					|| (fromDb.getTitle() != null && !urldata.getTitle().equals(fromDb.getTitle()))) {
				fromDb.setTitle(urldata.getTitle());
			}

			urlDAO.update(fromDb);
		} else {
			urlDAO.create(urldata);
		}
	}

}
