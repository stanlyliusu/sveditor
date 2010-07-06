package net.sf.sveditor.core.expr.eval;

import net.sf.sveditor.core.expr.parser.SVCondExpr;
import net.sf.sveditor.core.expr.parser.SVConstraintIfExpr;
import net.sf.sveditor.core.expr.parser.SVExpr;
import net.sf.sveditor.core.expr.parser.SVExprIterator;
import net.sf.sveditor.core.expr.parser.SVIdentifierExpr;
import net.sf.sveditor.core.expr.parser.SVImplicationExpr;
import net.sf.sveditor.core.expr.parser.SVInsideExpr;

public class SVExprConstantCheck extends SVExprIterator {
	private IValueProvider			fValueProvider;
	private boolean					fIsConstant;
	
	public SVExprConstantCheck(IValueProvider provider) {
		fValueProvider = provider;
	}
	
	public synchronized boolean isConstant(SVExpr expr) {
		fIsConstant = true;
		
		visit(expr);
	
		return fIsConstant;
	}
	

	@Override
	protected void cond(SVCondExpr expr) {
		// TODO Auto-generated method stub
		super.cond(expr);
	}

	@Override
	protected void constraint_if(SVConstraintIfExpr expr) {
		fIsConstant = false;
		// TODO Auto-generated method stub
		super.constraint_if(expr);
	}

	@Override
	protected void implication(SVImplicationExpr expr) {
		// TODO Auto-generated method stub
		super.implication(expr);
	}

	@Override
	protected void inside(SVInsideExpr expr) {
		// Skip: visit(expr.getLhs());
		for (SVExpr e : expr.getValueRangeList()) {
			visit(e);
		}
	}

	@Override
	protected void identifier(SVIdentifierExpr expr) {
		try {
			fValueProvider.get_value(expr.getIdStr());
		} catch (Exception e) {
			fIsConstant = false;
		}
	}
}