package mx.com.gunix.framework.security.domain.persistence;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import mx.com.gunix.framework.security.domain.GunixPersistentTokenRepository;

public class JdbcGunixPersistentTokenRepository extends JdbcDaoSupport implements GunixPersistentTokenRepository{

	// ~ Instance fields
	// ================================================================================================

	private String tokensBySeriesSql;
	private String insertTokenSql;
	private String updateTokenSql;
	private String removeUserTokensSql;
	private String createTableSql;
	private boolean createTableOnStartup;

	protected void initDao() {
		if (createTableOnStartup) {
			getJdbcTemplate().execute(createTableSql);
		}
	}

	public void createNewToken(PersistentRememberMeToken token) {
		getJdbcTemplate().update(insertTokenSql, token.getUsername(), token.getSeries(), token.getTokenValue(), token.getDate());
	}

	public void updateToken(String series, String tokenValue, Date lastUsed) {
		getJdbcTemplate().update(updateTokenSql, tokenValue, lastUsed, series);
	}

	/**
	 * Loads the token data for the supplied series identifier.
	 *
	 * If an error occurs, it will be reported and null will be returned (since
	 * the result should just be a failed persistent login).
	 *
	 * @param seriesId
	 * @return the token matching the series, or null if no match found or an
	 *         exception occurred.
	 */
	public PersistentRememberMeToken getTokenForSeries(String seriesId) {
		try {
			return getJdbcTemplate().queryForObject(tokensBySeriesSql, new RowMapper<PersistentRememberMeToken>() {
				public PersistentRememberMeToken mapRow(ResultSet rs, int rowNum) throws SQLException {
					return new PersistentRememberMeToken(rs.getString(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4));
				}
			}, seriesId);
		} catch (EmptyResultDataAccessException zeroResults) {
			if (logger.isDebugEnabled()) {
				logger.debug("Querying token for series '" + seriesId + "' returned no results.", zeroResults);
			}
		} catch (IncorrectResultSizeDataAccessException moreThanOne) {
			logger.error("Querying token for series '" + seriesId + "' returned more than one value. Series" + " should be unique");
		} catch (DataAccessException e) {
			logger.error("Failed to load token for series " + seriesId, e);
		}

		return null;
	}

	public void removeUserTokens(String username) {
		getJdbcTemplate().update(removeUserTokensSql, username);
	}

	/**
	 * Intended for convenience in debugging. Will create the persistent_tokens
	 * database table when the class is initialized during the initDao method.
	 *
	 * @param createTableOnStartup
	 *            set to true to execute the
	 */
	public void setCreateTableOnStartup(boolean createTableOnStartup) {
		this.createTableOnStartup = createTableOnStartup;
	}

	@Override
	public void init(String tokensBySeriesSql, String insertTokenSql, String updateTokenSql,
			String removeUserTokensSql, String createTableSql) {
		this.tokensBySeriesSql=tokensBySeriesSql;
		this.insertTokenSql=insertTokenSql;
		this.updateTokenSql=updateTokenSql;
		this.removeUserTokensSql=removeUserTokensSql;
		this.createTableSql=createTableSql;
	}
}