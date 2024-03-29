package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;

public class IDocBackup extends Thread {
	public static final String SYNC = "sync";
	public IDocServer iDocServer;
	public List errorIdocTypes = new LinkedList();
	public IDocType idocType;

	DataBaseUtil dbUtil;

	public IDocBackup(IDocServer iDocServer) {
		this.iDocServer = iDocServer;
	}

	public void run() {
		while (isServerRunning()) {
			idocType = null;
			IDocFile file = iDocServer.getBckupFile();
			if (file == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					iDocServer.log(e);
				}
			} else {
				try {
					dbUtil = iDocServer.getConnection();
					int header_id = insertInterface(file);
					dbUtil.updateIdocStatus(header_id, file.getIdocId(), "done");
					LoggerUtil.getLogger().log("Idoc File id=" + file.getIdocId() + " execute successful !");
				} catch (Throwable e) {
					try {
						iDocServer.log(e);
						if (idocType != null) {
							errorIdocTypes.add(idocType);
						}
						String errorMessage = "interface failed";
						LoggerUtil.getLogger().log("updateIdocStatus for idoc:" + file.getIdocId() + " " + errorMessage);
						dbUtil.updateIdocsStatus(file.getIdocId(), errorMessage);
					} catch (Throwable e1) {
						iDocServer.log(e1);
					}
				} finally {
					closeDatabaseUtil(dbUtil);
				}
			}
		}
	}

	private void closeDatabaseUtil(DataBaseUtil dataBaseUtil) {
		if (dataBaseUtil != null)
			dataBaseUtil.close();
	}

	private int insertInterface(IDocFile file) throws AuroraIDocException {
		int header_id = -1;
		try {
			header_id = dbUtil.existHeaders(file.getIdocId());
			if (header_id != -1) {
				CompositeLoader loader = new CompositeLoader();
				CompositeMap iDocData = loader.loadByFile(file.getPath());
				if (iDocData.getChilds() == null)
					return header_id;
				dbUtil.setConnectionAutoCommit(false);
				for (Iterator it = iDocData.getChildIterator(); it.hasNext();) {
					CompositeMap idoc_node = (CompositeMap) it.next();
					if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
						return header_id;
					}
					CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
					if (idocType == null) {
						idocType = dbUtil.getIdocType(control_node);
						if (isIdocTypeStop()) {
							throw new AuroraIDocException("This idocType:" + idocType + " has error before");
						}
						dbUtil.updateIdocInfo(file.getIdocId(), control_node);
					}
					for (int i = 1; i < idoc_node.getChilds().size(); i++) {
						CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(i);
						dbUtil.registerInterfaceLine(header_id, content_node);
					}
				}
				dbUtil.getConnection().commit();
			}
		} catch (Throwable e) {
			dbUtil.rollbackConnection();
			throw new AuroraIDocException(e);
		} finally {
			dbUtil.setConnectionAutoCommit(true);
		}
		if (iDocServer.isDeleteFileImmediately()) {
			File deleteFile = new File(file.getPath());
			if (deleteFile.exists()) {
				LoggerUtil.getLogger().log("delete file " + file.getPath() + " " + deleteFile.delete());
			}
		}
		return header_id;
	}

	public boolean isServerRunning() {
		return iDocServer.isRunning();
	}

	private boolean isIdocTypeStop() throws SQLException, AuroraIDocException {
		String handleModel = dbUtil.getHandleModel(idocType.getIdoctyp(), idocType.getCimtyp());
		if (SYNC.equals(handleModel) && errorIdocTypes.contains(idocType)) {
			return true;
		}
		return false;
	}
}
