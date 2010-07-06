/****************************************************************************
 * Copyright (c) 2008-2010 Matthew Ballance and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Ballance - initial implementation
 ****************************************************************************/


package net.sf.sveditor.core.db;

import net.sf.sveditor.core.db.persistence.DBFormatException;
import net.sf.sveditor.core.db.persistence.IDBReader;
import net.sf.sveditor.core.db.persistence.IDBWriter;
import net.sf.sveditor.core.db.persistence.ISVDBPersistenceFactory;
import net.sf.sveditor.core.db.persistence.SVDBPersistenceReader;

public class SVDBAlwaysBlock extends SVDBScopeItem {
	private String				fExpr;
	
	public static void init() {
		ISVDBPersistenceFactory f = new ISVDBPersistenceFactory() {
			public SVDBItem readSVDBItem(IDBReader reader, SVDBItemType type, 
					SVDBFile file, SVDBScopeItem parent) throws DBFormatException {
				return new SVDBAlwaysBlock(file, parent, type, reader);
			}
		};
		
		SVDBPersistenceReader.registerPersistenceFactory(f, SVDBItemType.AlwaysBlock); 
	}
	
	public SVDBAlwaysBlock(String expr) {
		super("", SVDBItemType.AlwaysBlock);
		fExpr = expr;
	}
	
	public SVDBAlwaysBlock(SVDBFile file, SVDBScopeItem parent, SVDBItemType type, IDBReader reader) throws DBFormatException {
		super(file, parent, type, reader);
		fExpr = reader.readString();
	}
	
	public String getExpr() {
		return fExpr;
	}
	
	public void setExpr(String expr) {
		fExpr = expr;
	}

	@Override
	public void dump(IDBWriter writer) {
		super.dump(writer);
		writer.writeString(fExpr);
	}

	@Override
	public SVDBItem duplicate() {
		SVDBAlwaysBlock ret = new SVDBAlwaysBlock(fExpr);
		
		ret.init(this);
		
		return ret;
	}

	@Override
	public void init(SVDBItem other) {
		super.init(other);
		fExpr = ((SVDBAlwaysBlock)other).fExpr;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof SVDBAlwaysBlock) {
			return ((SVDBAlwaysBlock)obj).fExpr.equals(fExpr) &&
					super.equals(obj);
		}
		return false;
	}
	
}