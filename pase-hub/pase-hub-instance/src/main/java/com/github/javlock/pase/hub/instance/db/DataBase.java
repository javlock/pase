package com.github.javlock.pase.hub.instance.db;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javlock.pase.hub.instance.PaseHub;
import com.github.javlock.pase.hub.instance.config.db.DataBaseConfig;
import com.github.javlock.pase.libs.data.RegExData;
import com.github.javlock.pase.libs.data.web.UrlData;
import com.github.javlock.pase.libs.data.web.UrlData.URLTYPE;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.TableUtils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;

@SuppressFBWarnings(value = { "EI_EXPOSE_REP2" })
public class DataBase {
	private static final String PAGE_TYPE = "pageType";
	private static final Logger LOGGER = LoggerFactory.getLogger("DataBase");
	private PaseHub hub;

	private JdbcPooledConnectionSource connectionSource;
	private Dao<UrlData, Integer> urlDAO;
	private @Getter Dao<RegExData, String> regExDAO;

	public DataBase(PaseHub instanceHub) {
		hub = instanceHub;
	}

	void createDAOs() throws SQLException {
		urlDAO = DaoManager.createDao(connectionSource, UrlData.class);// urls
		regExDAO = DaoManager.createDao(connectionSource, RegExData.class);// regEx
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
		createTableFor(regExDAO, RegExData.class);
	}

	public List<UrlData> getUrlFilesNoParsed() throws SQLException {
		ArrayList<UrlData> resp = new ArrayList<>();
		QueryBuilder<UrlData, Integer> queryBuilder = urlDAO.queryBuilder();
		Where<UrlData, Integer> where = queryBuilder.where();

		where.eq(PAGE_TYPE, UrlData.URLTYPE.FILE);

		List<UrlData> listUrls = queryBuilder.query();
		for (UrlData urlData : listUrls) {
			if (hub.getFilterEngine().check(urlData)) {
				// TODO отсеять уже разобранные файлы
			}

		}

		return resp;
	}

	public List<UrlData> getUrlNew() throws SQLException {
		ArrayList<UrlData> resp = new ArrayList<>();
		QueryBuilder<UrlData, Integer> queryBuilder = urlDAO.queryBuilder();
		Where<UrlData, Integer> where = queryBuilder.where();

		where.eq(PAGE_TYPE, UrlData.URLTYPE.UKNOWN);
		queryBuilder.limit(300L);

		List<UrlData> listUrls = queryBuilder.query();
		for (UrlData urlData : listUrls) {
			if (hub.getFilterEngine().check(urlData)) {
				resp.add(urlData);
			}
		}

		return resp;
	}

	public List<UrlData> getUrlTimeExceeded() throws SQLException {
		ArrayList<UrlData> resp = new ArrayList<>();
		QueryBuilder<UrlData, Integer> queryBuilder = urlDAO.queryBuilder();
		Where<UrlData, Integer> where = queryBuilder.where();

		where.eq(PAGE_TYPE, UrlData.URLTYPE.PAGE);
		queryBuilder.limit(300L);

		List<UrlData> listUrls = queryBuilder.query();
		for (UrlData urlData : listUrls) {
			long time = System.currentTimeMillis() / 1000;
			Long configTime = hub.getConfig().getTimeExceeded();
			Long uTime = urlData.getTime();

			if (uTime == null) {// бред
				uTime = 0L;
				urlData.setTime(0L);
			}

			long checkTime = uTime + configTime;
			if (checkTime <= time) {
				if (hub.getFilterEngine().check(urlData)) {
					resp.add(urlData);
				}
			}
		}
		return resp;
	}

	public void init() throws SQLException {
		createSource();
		createDAOs();
		createTables();

		initData();

		readSettingsFromDb();
	}

	private void initData() throws SQLException {
		if (regExDAO.countOf() == 0) {
			RegExData allowAll = new RegExData().setRegEx(".*").setAllow(true).setEnabled(true).build();
			RegExData denyGov = new RegExData().setRegEx(".*\\.gov\\..*").setDeny(true).setEnabled(true).build();

			regExDAO.create(denyGov);
			regExDAO.create(allowAll);
			hub.getFilterEngine().updateFilter(denyGov);
			hub.getFilterEngine().updateFilter(allowAll);
		}

	}

	private void readSettingsFromDb() {
		// users
		// network
		// threads

		// regEx
		for (RegExData regExData : regExDAO) {

			hub.getFilterEngine().updateFilter(regExData);
		}

	}

	public void saveUrlData(UrlData urldata) throws SQLException {
		if (urlDAO.idExists(urldata.getHashId())) {
			saveUrlDataExist(urldata);
		} else {
			urlDAO.create(urldata);
			LOGGER.info("saved:{}", urldata);
		}
	}

	private void saveUrlDataExist(UrlData urldata) throws SQLException {
		UrlData fromDb = urlDAO.queryForId(urldata.getHashId());
		boolean updated = false;

		// PAGETYPE
		if (fromDb.getPageType() != urldata.getPageType() && !urldata.getPageType().equals(URLTYPE.UKNOWN)) {
			fromDb.setPageType(urldata.getPageType());
			updated = true;
		}
		// TITLE
		String fdbTitle = fromDb.getTitle();
		String uTitle = urldata.getTitle();

		if (uTitle != null) {
			if (fdbTitle == null) {
				fromDb.setTitle(uTitle);
				updated = true;
			}
			if (fdbTitle != null && !fdbTitle.equals(uTitle)) {
				fromDb.setTitle(uTitle);
				updated = true;
			}
		}

		// TIME
		Long fTime = fromDb.getTime();
		Long uTime = urldata.getTime();
		if (uTime != null) {
			if (fTime == null) {
				fromDb.setTime(uTime);
				updated = true;
			}
			if (fTime != null && (uTime > fTime)) {
				fromDb.setTime(uTime);
				updated = true;
			}
		}

		// LAST >STATUSCODE<
		if (fromDb.getStatusCode() != urldata.getStatusCode()) {
			fromDb.setStatusCode(urldata.getStatusCode());
			updated = true;

		}
		if (updated) {
			urlDAO.update(fromDb);
		}
	}

}
