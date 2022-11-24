/**
 * 
 */
package ca.datamagic.accounting.testing;

import java.io.File;

import org.junit.BeforeClass;

import ca.datamagic.accounting.dao.BaseDAO;

/**
 * @author gregm
 *
 */
public abstract class BaseTester {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		String dataPath = (new File("src/test/resources")).getCanonicalPath();
		BaseDAO.setDataPath(dataPath);
	}
}
