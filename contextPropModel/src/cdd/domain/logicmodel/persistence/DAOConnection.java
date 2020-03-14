package cdd.domain.logicmodel.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DAOConnection {

	private Connection jdbcConn;

	private boolean closed = false;

	public void setConnectionJDBC(final Connection jdbcConn_) {
		this.jdbcConn = jdbcConn_;
	}

	public Connection getJdbcConn() {
		return this.jdbcConn;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public void close() throws SQLException {
		this.jdbcConn.close();
		this.closed = true;
	}

	public void setAutoCommit(final boolean s) throws SQLException {
		this.jdbcConn.setAutoCommit(s);
	}

	public void commit() throws SQLException {
		this.jdbcConn.commit();
	}

	public void rollback() throws SQLException {
		this.jdbcConn.rollback();
	}

	public PreparedStatement prepareStatement(final String sql) throws SQLException {
		return this.jdbcConn.prepareStatement(sql);
	}

	public boolean isResourceLocked(Throwable sqlExc) {
		Throwable stackException = sqlExc.getCause();
		while (stackException != null) {
			if (stackException.getMessage().indexOf("database is locked") != -1) {
				return true;
			}
			stackException = stackException.getCause();
		}
		return false;
	}

}
