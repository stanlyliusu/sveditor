/****************************************************************************
 * Copyright (c) 2008-2014 Matthew Ballance and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Ballance - initial implementation
 ****************************************************************************/


package net.sf.sveditor.core.tests;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;

import net.sf.sveditor.core.StringInputStream;
import net.sf.sveditor.core.db.index.ISVDBFileSystemChangeListener;
import net.sf.sveditor.core.db.index.ISVDBFileSystemProvider;
import net.sf.sveditor.core.db.index.old.SVDBLibIndex;

public class SVDBStringDocumentIndex extends SVDBLibIndex {
	
	public SVDBStringDocumentIndex(final String input) {
		super("STUB", "ROOT", new ISVDBFileSystemProvider() {
			public InputStream openStream(String path) {
				if (path.equals("ROOT")) {
					return new StringInputStream(input);
				} else {
					return null;
				}
			}
			public OutputStream openStreamWrite(String path) { return null; }
			public boolean fileExists(String path) {
				return path.equals("ROOT");
			}
			public boolean isDir(String path) {
				// Unsure
				return false;
			}
			public List<String> getFiles(String path) {
				return new ArrayList<String>();
			}
			public void init(String root) {}
			public long getLastModifiedTime(String path) {return 0;}
			public String resolvePath(String path, String fmt) {return path;}
			public void removeFileSystemChangeListener(ISVDBFileSystemChangeListener l) {}
			public void dispose() {}
			public void closeStream(InputStream in) {}
			public void closeStream(OutputStream out) {}
			public void clearMarkers(String path) {}
			public void addMarker(String path, String type, int lineno, String msg) {}
			public void addFileSystemChangeListener(ISVDBFileSystemChangeListener l) {}
		}, TestIndexCacheFactory.instance().createIndexCache("__", "__"), null);
		init(new NullProgressMonitor(), null);
	}
}
