package org.hibernate.eclipse.console.wizards.encryption;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.refactoring.nls.changes.CreateTextFileChange;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.internal.ui.refactoring.PreviewWizardPage;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.hibernate.eclipse.console.HibernateConsolePlugin;
import org.hibernate.eclipse.console.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Preview wizard page for new hibernate mappings.
 *
 * @author Ricardo Rodriguez
 */
@SuppressWarnings("restriction")
public class NewRevEngPreviewPage extends PreviewWizardPage {

	public static final String HIBERNATE_NEW_REVENG_XML_FOLDER_NAME = "hibernateNewRevEngXml"; //$NON-NLS-1$
	public static final Logger log=LoggerFactory.getLogger(NewRevEngPreviewPage.class);
	
	protected IPath rootPlace2GenBase = null;
	protected IPath rootPlace2Gen = null;
	protected Set<IPath> paths2Disconnect = new HashSet<IPath>();

	public NewRevEngPreviewPage() {
		super(true);
	}
	
	@Override
	public void dispose() {
		performDisconnect();
		IPath place2Gen = getRootPlace2Gen();
		if (place2Gen != null) {
			File folder2Gen = new File(place2Gen.toOSString());
			FileUtils.delete(folder2Gen);
		}
		super.dispose();
	}

	/**
	 * Disconnects files collected at paths2Disconnect from file manager
	 */
	protected void performDisconnect() {
		final ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		for (IPath filePathTo_Show : paths2Disconnect) {
			try {
				log.debug("Disconnecting "+filePathTo_Show); //$NON-NLS-1$
				bufferManager.disconnect(filePathTo_Show, LocationKind.IFILE, null);
			} catch (CoreException e) {
				HibernateConsolePlugin.getDefault().logErrorMessage("CoreException: ", e); //$NON-NLS-1$
			}
		}
		paths2Disconnect.clear();
	}
	
	/**
	 * Perform textFileChanges
	 */
	protected void performCommit() {
		log.debug("perform textFileChanges changes"); //$NON-NLS-1$
		final CompositeChange cc = (CompositeChange)getChange();
		if (cc == null) {
			return;
		}
		final ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
		Change[] changes = cc.getChildren();
		for (int i = 0; i < changes.length; i++) {
			Change change = changes[i];
			if (!(change instanceof TextFileChange)) {
				continue;
			}
			TextFileChange tfc = (TextFileChange)change;
			if (tfc.isEnabled() && tfc.getEdit() != null) {
				IPath path = new Path(tfc.getName());
				ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
				IDocument document = textFileBuffer.getDocument();
				try {
					tfc.getEdit().apply(document);
				} catch (MalformedTreeException e) {
					HibernateConsolePlugin.getDefault().logErrorMessage("MalformedTreeException: ", e); //$NON-NLS-1$
				} catch (BadLocationException e) {
					HibernateConsolePlugin.getDefault().logErrorMessage("BadLocationException: ", e); //$NON-NLS-1$
				}
				try {
					// commit changes to underlying file
					textFileBuffer.commit(null, true);
				} catch (CoreException e) {
					HibernateConsolePlugin.getDefault().logErrorMessage("CoreException: ", e); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * The function reads file content into the string.
	 * @param fileSrc
	 * @return
	 */
	protected String readInto(File fileSrc) {
		FileInputStream fis = null;
		BufferedInputStream bis = null;
		StringBuilder str = new StringBuilder();
		try {
			fis = new FileInputStream(fileSrc);
			bis = new BufferedInputStream(fis);
	        byte[] buff = new byte[1<<14];
			while (true) {
				int n = -1;
				try {
					n = bis.read(buff);
				} catch (IOException e) {
					HibernateConsolePlugin.getDefault().log(e);
				}
				if (n == -1) {
					break;
				}
				str.append(new String(buff, 0, n));
			}
		} catch (FileNotFoundException e) {
			HibernateConsolePlugin.getDefault().log(e);
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (IOException e) {}
			}
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {}
			}
		}
		return str.toString();
	}

	/**
	 * @return Path to a the temporary folder "hibernateNewHbmXml" to place the hbm.xml mapping files 
	 */
	private IPath getRootPlace2GenBase() {
		if (rootPlace2GenBase != null) {
			return rootPlace2GenBase;
		}
		String systemTmpDir = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		rootPlace2GenBase = new Path(systemTmpDir);
		rootPlace2GenBase = rootPlace2GenBase.append(HIBERNATE_NEW_REVENG_XML_FOLDER_NAME);
		return rootPlace2GenBase;
	}

	/**
	 * Get a temporary folder with a unique id to place the hbm.xml files
	 * @return
	 */
	public IPath getRootPlace2Gen() {
		if (rootPlace2Gen != null) {
			return rootPlace2Gen;
		}
		rootPlace2Gen = getRootPlace2GenBase();
		String uuidName = UUID.randomUUID().toString();
		rootPlace2Gen = rootPlace2Gen.append(uuidName);
		return rootPlace2Gen;
	}
	
	/**
	 * Try to create one change according with input file (fileSrc).
	 * In case of success change be added into cc and returns true.
	 * @param cc
	 * @param proj
	 * @param fileSrc
	 * @return true if the change was created successfully 
	 */
	public boolean updateOneChange(final CompositeChange cc, final IPath path, String fileSrc) {
		log.debug("Creating change for file: " +path); //$NON-NLS-1$
		boolean res = false;

		//cc.
		if (fileSrc.isEmpty()) {
			return res;
		}	
		IFile file=IDEWorkbenchPlugin.getPluginWorkspace().getRoot().getFile(path);
		if (file.getLocation().toFile().isDirectory()) {
			return res;
		}
		if (!file.exists()) {
			log.debug("Create textFileChange/resourceChange for new hbm.xml "+path.toString()); //$NON-NLS-1$

			CreateTextFileChange change = new CreateTextFileChange(path, fileSrc.toString(), "UTF-8", "hbm.xml"); //$NON-NLS-1$
			cc.add(change);
			//
			res = true;
		}else {
			log.debug("Resouce already exist on project "+path); //$NON-NLS-1$
			final ITextFileBufferManager bufferManager = FileBuffers.getTextFileBufferManager();
			ITextFileBuffer textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			if (textFileBuffer == null) {
				try {
					bufferManager.connect(path, LocationKind.IFILE, null);
					paths2Disconnect.add(path);
				} catch (CoreException e) {
					HibernateConsolePlugin.getDefault().logErrorMessage("CoreException: ", e); //$NON-NLS-1$
				}
				textFileBuffer = bufferManager.getTextFileBuffer(path, LocationKind.IFILE);
			}
			if (textFileBuffer != null) {
				
				IDocument documenToChange = textFileBuffer.getDocument();
				//
				log.debug("Create textEdit change to replace the content of "+path); //$NON-NLS-1$
				TextEdit textEdit = new ReplaceEdit(0, documenToChange.getLength(), fileSrc);
				//
				TextFileChange change = new TextFileChange(path.toOSString(), file);
				change.setSaveMode(TextFileChange.LEAVE_DIRTY);
				change.setEdit(textEdit);
				cc.add(change);
				//
				res = true;
			}
		} 
		return res;
	}
	
	/**
	 * Try to create changes according with all files in the input directory (dir).
	 * Changes be added into cc.
	 * @param cc
	 * @param proj
	 * @param dir
	 */
//	protected void updateChanges(final CompositeChange cc, final IJavaProject proj, File dir) {
//		if (!dir.exists()) {
//			return;
//		}
//		if (!dir.isDirectory()) {
//			updateOneChange(cc, proj, dir);
//			return;
//		}
//		File[] files = dir.listFiles();
//		for (int i = 0; i < files.length; i++) {
//			if (files[i].isDirectory()) {
//				updateChanges(cc, proj, files[i]);
//			} else {
//				updateOneChange(cc, proj, files[i]);
//			}
//		}
//	}
//	

	/**
	 * Try to create a composite change set, from the entries in places2Gen
	 */
	public void resetChanges() {
		performDisconnect();
		
	}
	
	/**
	 * Apply changes.
	 */
	@Override
	public boolean performFinish() {
		if (getChange() == null) {
			return false;
		}
		performCommit();
		try {
			getChange().perform(new NullProgressMonitor());
		} catch (CoreException e) {
			HibernateConsolePlugin.getDefault().logErrorMessage("CoreException: ", e); //$NON-NLS-1$
		}
		performDisconnect();
		return true;
	}
}
