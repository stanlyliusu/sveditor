/****************************************************************************
 * Copyright (c) 2008-2011 Matthew Ballance and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthew Ballance - initial implementation
 ****************************************************************************/


package net.sf.sveditor.core.expr_utils;

import net.sf.sveditor.core.db.ISVDBItemBase;

public interface ISVItemResolver {
	
	ISVDBItemBase resolveItemScopeRelative(String name);
	
	ISVDBItemBase resolveItemBaseRelative(ISVDBItemBase base, String name);

}
