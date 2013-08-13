package aurora.plugin.sap.sync.idoc;

import java.io.File;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uncertain.composite.CompositeLoader;
import uncertain.composite.CompositeMap;

public class IDocSync extends Thread {
	public static final String SYNC = "sync";
	public IDocServer iDocServer;
	public List errorIdocTypes = new LinkedList();
	public IDocType idocType;

	DataBaseUtil dbUtil;

	public IDocSync(IDocServer iDocServer) {
		this.iDocServer = iDocServer;
	}

	public void run() {
		while (isServerRunning()) {
			idocType = null;
			IDocFile file = iDocServer.getSyncFile();
			if (file == null) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					iDocServer.log(e);
				}
			} else {
				int header_id = -1;
				String context = "middle";
				try {
					dbUtil = iDocServer.getConnection();
					header_id = insertMiddleTables(file);
					context = "formal";
					insertFormalTables(file, header_id);

					if (iDocServer.isEnableInterfaceHistory())
						iDocServer.addBackupFile(file);
					else if (iDocServer.isDeleteFileImmediately()) {
						File deleteFile = new File(file.getPath());
						if (deleteFile.exists()) {
							LoggerUtil.getLogger().log("delete file " + file.getPath() + " " + deleteFile.delete());
						}
					}
				} catch (Throwable e) {
					iDocServer.log(e);
					try {
						String errorMessage = context + " failed";
						LoggerUtil.getLogger().log("updateIdocStatus for idoc:" + file.getIdocId() + " " + errorMessage);
						dbUtil.updateIdocStatus(header_id, file.getIdocId(), errorMessage);
					} catch (AuroraIDocException e1) {
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

	private int insertMiddleTables(IDocFile file) throws AuroraIDocException {
		int header_id = dbUtil.existHeaders(file.getIdocId());
		try {
			if (header_id == -1) {
				CompositeLoader loader = new CompositeLoader();
				CompositeMap iDocData = loader.loadByFile(file.getPath());
				if (iDocData.getChilds() == null)
					return header_id;
				// dbUtil.setConnectionAutoCommit(false);
				for (Iterator it = iDocData.getChildIterator(); it.hasNext();) {
					CompositeMap idoc_node = (CompositeMap) it.next();
					if (idoc_node == null || idoc_node.getChildIterator() == null || idoc_node.getChilds().size() < 2) {
						return header_id;
					}
					CompositeMap control_node = (CompositeMap) idoc_node.getChilds().get(0);
					if (header_id == -1) {
						idocType = dbUtil.getIdocType(control_node);
						if (isIdocTypeStop()) {
							throw new AuroraIDocException("This idocType:" + idocType + " has error before");
						}
						header_id = dbUtil.registerInterfaceHeader(file.getIdocId(), control_node);
						dbUtil.updateIdocInfo(file.getIdocId(), control_node);
					}
					for (int i = 1; i < idoc_node.getChilds().size(); i++) {
						CompositeMap content_node = (CompositeMap) idoc_node.getChilds().get(i);
						dbUtil.registerMiddleLine(header_id, content_node);
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
		return header_id;
	}

	private void insertFormalTables(IDocFile file, int header_id) throws SQLException, AuroraIDocException {
		String executePkg = dbUtil.getFormalExecutePkg(file.getIdocId());
		String errorMessage = dbUtil.executePkg(executePkg, header_id);
		if (errorMessage != null && !"".equals(errorMessage)) {
			throw new AuroraIDocException("execute Formal Pkg " + executePkg + " failed:" + errorMessage);
		}
		dbUtil.updateIdocStatus(header_id, file.getIdocId(), "done");
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
