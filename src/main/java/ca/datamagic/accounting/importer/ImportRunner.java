/**
 * 
 */
package ca.datamagic.accounting.importer;

/**
 * @author Greg
 *
 */
public class ImportRunner implements Runnable {
	private String date = null;
	
	public ImportRunner(String date) {
		this.date = date;
	}
	
	@Override
	public void run() {
		Importer importer = new Importer(date);
		importer.importFile();
	}
}
