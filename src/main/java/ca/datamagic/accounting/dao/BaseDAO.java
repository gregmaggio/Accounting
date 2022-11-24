/**
 * 
 */
package ca.datamagic.accounting.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.collect.Lists;

import ca.datamagic.accounting.util.IOUtils;

/**
 * @author Greg
 *
 */
public abstract class BaseDAO {
	private static String dataPath = "C:/Dev/Accounting/src/main/resources";
	private Properties properties = null;
	private GoogleCredentials credentials = null;
	
	public static String getDataPath() {
		return dataPath;
	}
	
	public static void setDataPath(String newVal) {
		dataPath = newVal;
	}
	
	public Properties getProperties() throws IOException {
		if (this.properties == null) {
			InputStream inputStream = null;
			try {
				inputStream = new FileInputStream(MessageFormat.format("{0}/secure.properties", getDataPath()));
				Properties properties = new Properties();
				properties.load(inputStream);
				this.properties = properties;
			} finally {
				IOUtils.closeQuietly(inputStream);
			}
		}
		return this.properties;
	}
	
	public GoogleCredentials getCredentials() throws IOException {
		if (this.credentials == null) {
			this.credentials = GoogleCredentials.fromStream(new FileInputStream(MessageFormat.format("{0}/secure.json", dataPath))).createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
		}
		return this.credentials;
	}
}
