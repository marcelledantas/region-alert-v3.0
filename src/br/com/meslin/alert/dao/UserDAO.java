/**
 * 
 */
package br.com.meslin.alert.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import br.com.meslin.alert.model.Person;

/**
 * @author meslin
 *
 */
public class UserDAO implements DAO<Person> {
	private static UserDAO instance;
	/*
	 * MySQL
	 */
//	private final static String CLASSNAME = "com.mysql.jdbc.Driver";
//	private final static String DBURL = "jdbc:mysql://alert/CITYALERT?user=ROOT&password=alert";
	/*
	 * SQLite
	 */
	private final static String CLASSNAME = "org.sqlite.JDBC";
	private final static String DBURL = "jdbc:sqlite:REGIONAlert.db";
	private Connection connection;
	
	/**
	 * 
	 */
	private UserDAO() {
		try {
			Class.forName(CLASSNAME);
			this.connection = DriverManager.getConnection(DBURL);
		} catch (ClassNotFoundException | SQLException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static synchronized UserDAO getInstance() {
		if(instance == null) {
			instance = new UserDAO();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#get(long)
	 */
	@Override
	public Optional<Person> get(long id) {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#getAll()
	 */
	@Override
	public List<Person> getAll() {
		// Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#save(java.lang.Object)
	 */
	@Override
	public void save(Person t) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#update(java.lang.Object, java.lang.String[])
	 */
	@Override
	public void update(Person t, String[] params) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#delete(java.lang.Object)
	 */
	@Override
	public void delete(Person t) {
		// Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see br.com.meslin.alert.dao.DAO#getAreasByUsername(java.lang.String)
	 */
	@Override
	public List<Integer> getAreasByUsername(String username) throws ClassNotFoundException, SQLException {
		ResultSet resultSet;
		List<Integer> areas = new ArrayList<Integer>();
		final String SQL = "select * from AREA where fk_USERNAME = ?";
		PreparedStatement preparedStatement = connection.prepareStatement(SQL);
		int pos =1;
		preparedStatement.setString(pos++, username);
		resultSet = preparedStatement.executeQuery();
		while(resultSet.next()) {
			areas.add(resultSet.getInt("AREA"));
		}

		return areas;
	}

}
