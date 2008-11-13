package org.xidea.el;

import org.xidea.el.parser.OperatorToken;



public interface Calculater {
	// 做2值之间的计算
	/**
	 * @param it 
	 * @return skip next value
	 * @see CalculaterImpl
	 */
	public Object compute(OperatorToken op,Object arg1,Object arg2) ;
}