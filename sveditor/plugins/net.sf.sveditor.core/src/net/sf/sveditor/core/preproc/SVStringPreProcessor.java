package net.sf.sveditor.core.preproc;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.sveditor.core.db.SVDBMacroDef;
import net.sf.sveditor.core.scanner.IPreProcMacroProvider;

public class SVStringPreProcessor implements ISVStringPreProcessor, IPreProcMacroProvider {

	private Map<String, SVDBMacroDef>	fMacroMap;
	private IPreProcMacroProvider		fMacroProvider;
	private boolean						fLocked;
	
	public SVStringPreProcessor(List<SVDBMacroDef> incoming_macros) {
		fMacroMap = new HashMap<String, SVDBMacroDef>();
		for (SVDBMacroDef m : incoming_macros) {
			addMacro(m);
		}
		fLocked = true;
	}
	
	public SVStringPreProcessor(SVDBMacroDef ... incoming_macros) {
		fMacroMap = new HashMap<String, SVDBMacroDef>();
		for (SVDBMacroDef m : incoming_macros) {
			addMacro(m);
		}
		fLocked = true;
	}
	
	public SVStringPreProcessor(IPreProcMacroProvider macro_provider) {
		fMacroProvider = macro_provider;
		fLocked = true;
	}

	@Override
	public String preprocess(InputStream in) {
		SVPreProcessor2 preproc = new SVPreProcessor2("", in, null, null);
		preproc.setMacroProvider(this);
		
		SVPreProcOutput out = preproc.preprocess();
		
		return out.toString();
	}

	@Override
	public SVDBMacroDef findMacro(String name, int lineno) {
		if (fMacroProvider != null) {
			return fMacroProvider.findMacro(name, lineno);
		} else {
			return fMacroMap.get(name);
		}
	}

	@Override
	public void addMacro(SVDBMacroDef macro) {
		if (!fLocked) {
			if (fMacroProvider != null) {
				fMacroProvider.addMacro(macro);
			} else {
				if (fMacroMap.containsKey(macro.getName())) {
					fMacroMap.remove(macro.getName());
				}
				fMacroMap.put(macro.getName(), macro);
			}
		}
	}

	@Override
	public void setMacro(String key, String value) {
		addMacro(new SVDBMacroDef(key, value));
	}
	
}
