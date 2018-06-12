package im.dao.impl;

import javax.sql.DataSource;

import java.util.Properties;

import org.apache.ibatis.datasource.DataSourceFactory;

import com.zaxxer.hikari.HikariDataSource;

/**
 * HikariCPDataSource
 * Date: 2018-06-11
 *
 * @author zouchuanhua
 */
public class HikariCPDataSource implements DataSourceFactory {

    private Properties props;

    @Override
    public DataSource getDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl(props.getProperty("url"));
        hikariDataSource.setDriverClassName(props.getProperty("driver"));
        hikariDataSource.setUsername(props.getProperty("username"));
        hikariDataSource.setPassword(props.getProperty("password"));
        return hikariDataSource;
    }

    @Override
    public void setProperties(Properties props) {
        this.props = props;
    }
}
