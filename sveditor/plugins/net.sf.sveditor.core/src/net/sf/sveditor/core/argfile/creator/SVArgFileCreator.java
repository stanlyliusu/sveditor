package net.sf.sveditor.core.argfile.creator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.sveditor.core.SVCorePlugin;
import net.sf.sveditor.core.SVFileUtils;
import net.sf.sveditor.core.StringInputStream;
import net.sf.sveditor.core.Tuple;
import net.sf.sveditor.core.db.SVDBFileTreeMacroList;
import net.sf.sveditor.core.db.index.ISVDBFileSystemProvider;
import net.sf.sveditor.core.log.LogFactory;
import net.sf.sveditor.core.log.LogHandle;
import net.sf.sveditor.core.preproc.ISVPreProcIncFileProvider;
import net.sf.sveditor.core.preproc.SVPreProcessor2;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * Creates an argument file by locating files 
 * within source folders
 * 
 * @author ballance
 *
 */
public class SVArgFileCreator implements ISVPreProcIncFileProvider {
	
	static class SrcFileInfo {
		public String			fPath;
		public boolean			fRootFile;
		public boolean			fArgFile;
	
		public SrcFileInfo(String path, boolean argfile) {
			fPath = path;
			fArgFile = argfile;
			reset();
		}
		
		public SrcFileInfo(String path) {
			this(path, false);
		}
		
		public void reset() {
			fRootFile = true;
		}
	}

	private LogHandle					fLog;
	private ISVDBFileSystemProvider		fFSProvider;
	private List<String>				fSearchPaths;
	private Set<String>					fFileExts;
	private Set<String>					fArgFileExts;
	private Set<String>					fDiscoveredPaths;
	private Set<String>					fIncDirs;
	private List<SrcFileInfo>			fFileList;
	private SrcFileInfo					fActiveSrcFile;
	
	public SVArgFileCreator(ISVDBFileSystemProvider fs_provider) {
		fFSProvider = fs_provider;
		fSearchPaths = new ArrayList<String>();
		fFileExts = new HashSet<String>();
		fArgFileExts = new HashSet<String>();
		fFileList = new ArrayList<SVArgFileCreator.SrcFileInfo>();
		fDiscoveredPaths = new HashSet<String>();
		fIncDirs = new HashSet<String>();
		
		fLog = LogFactory.getLogHandle("SVArgFileCreator");

		for (String ext : SVCorePlugin.getDefault().getDefaultSVExts()) {
			fFileExts.add(ext);
		}
		
		for (String ext : SVCorePlugin.getDefault().getDefaultArgFileExts()) {
			fArgFileExts.add(ext);
		}
	}
	
	public Set<String> getDiscoveredPaths() {
		return fDiscoveredPaths;
	}
	
	public void addSearchPath(String path) {
		fSearchPaths.add(path);
	}
	
	public void setSearchPaths(List<String> paths) {
		fSearchPaths.clear();
		fSearchPaths.addAll(paths);
	}
	
	public String getFileExts() {
		StringBuilder ret = new StringBuilder();
		
		for (String ext : fFileExts) {
			ret.append(ext + ", ");
		}
		ret.setLength(ret.length()-2);
		
		return ret.toString();
	}
	
	public void setFileExts(String exts) {
		String ext_l[] = exts.split(",");
	
		fFileExts.clear();
		for (String e : ext_l) {
			e = e.trim();
			
			if (!e.equals("")) {
				fFileExts.add(e);
			}
		}
	}

	/**
	 * Runs the file-discovery process 
	 * 
	 * Pre-conditions: 
	 *   - Specify file paths to explore
	 *   - Specify file extensions to accept
	 * @param monitor
	 */
	public void discover_files(IProgressMonitor monitor) {
		monitor.beginTask("Processing Root Files", 1000*fSearchPaths.size());
		fDiscoveredPaths.clear();
		fFileList.clear();
		
		for (String path : fSearchPaths){
			if (fFSProvider.isDir(path)) {
				discover_files(new SubProgressMonitor(monitor, 1000), path);
			} else {
				String ext = SVFileUtils.getPathExt(path);
				
				if (ext != null) {
					if (fFileExts.contains(ext)) {
						if (fDiscoveredPaths.add(path)) {
							fFileList.add(new SrcFileInfo(path));
						}
					} else if (fArgFileExts.contains(ext)) {
						if (fDiscoveredPaths.add(path)) {
							fFileList.add(new SrcFileInfo(path, true));
						}

					}
				}
			}
			
			if (monitor.isCanceled()) {
				break;
			}
		}
		
		monitor.done();
	}
	
	private void discover_files(IProgressMonitor monitor, String path) {
		List<String> paths = fFSProvider.getFiles(path);
		monitor.beginTask("Processing " + path, 100*paths.size());
		
		for (String p : paths) {
			if (fFSProvider.isDir(p)) {
				discover_files(new SubProgressMonitor(monitor, 100), p);
				if (monitor.isCanceled()) {
					break;
				}
			} else {
				String ext = SVFileUtils.getPathExt(p);
			
				if (ext != null) {
					if (fFileExts.contains(ext)) {
						if (fDiscoveredPaths.add(p)) {
							// Only add paths that we haven't encountered yet
							fFileList.add(new SrcFileInfo(p));
						}
					} else if (fArgFileExts.contains(ext)) {
						if (fDiscoveredPaths.add(p)) {
							fFileList.add(new SrcFileInfo(p, true));
						}
					}
				}
				monitor.worked(100);
			}
		}
		
		monitor.done();
	}

	/**
	 * Runs the pre-processor on all files to discovery
	 * which are root files and which are included by others
	 * 
	 * @param monitor
	 */
	public void organize_files(IProgressMonitor monitor) {
		monitor.beginTask("Processing Files", 1000*fFileList.size());
		
		fIncDirs.clear();
		for (SrcFileInfo info : fFileList) {
			info.reset();
		}
		
		for (SrcFileInfo info : fFileList) {
			if (!info.fArgFile) { // No point in parsing argument files
				fActiveSrcFile = info;
				InputStream in = fFSProvider.openStream(info.fPath);
				if (in == null) {
					fLog.error("Failed to open file \"" + info.fPath + "\"");
					continue;
				}

				SVPreProcessor2 pp = new SVPreProcessor2(info.fPath, in, this, null);

				pp.preprocess();
			}
		}
	
		monitor.done();
	}
	
	public List<String> getRootFiles() {
		List<String> ret = new ArrayList<String>();
		
		for (SrcFileInfo info : fFileList) {
			if (info.fRootFile && !info.fArgFile) {
				ret.add(info.fPath);
			}
		}
		
		return ret;
	}
	
	public List<String> getFiles() {
		List<String> ret = new ArrayList<String>();
		
		for (SrcFileInfo info : fFileList) {
			if (!info.fArgFile) {
				ret.add(info.fPath);
			}
		}
		
		return ret;
	}
	
	public List<String> getArgFiles() {
		List<String> ret = new ArrayList<String>();
		
		for (SrcFileInfo info : fFileList) {
			if (info.fArgFile) {
				ret.add(info.fPath);
			}
		}
		
		return ret;
	}
	
	public List<String> getIncDirs() {
		List<String> ret = new ArrayList<String>();
		
		for (String inc : fIncDirs) {
			ret.add(inc);
		}
		
		return ret;
	}

	@Override
	public Tuple<String, List<SVDBFileTreeMacroList>> findCachedIncFile(String incfile) {
		return null;
	}

	@Override
	public void addCachedIncFile(String incfile, String rootfile) { }

	@Override
	public Tuple<String, InputStream> findIncFile(String incfile) {
		Tuple<String, InputStream> ret = null;
		
		for (SrcFileInfo info : fFileList) {
			if (info == fActiveSrcFile) {
				continue;
			}
			if (info.fPath.endsWith(incfile)) {
				// Confirm...
				if ((info.fPath.length() == incfile.length()) ||
						info.fPath.charAt(info.fPath.length()-incfile.length()-1) == '/') {
					ret = new Tuple<String, InputStream>(info.fPath, new StringInputStream(""));
					// Since this file is included, it's not a root file
					info.fRootFile = false;
					String incdir = SVFileUtils.getPathParent(info.fPath);
					fIncDirs.add(incdir);
					break;
				}
			}
		}
		
		return ret;
	}

}
