/**
 * Author:Xiaoyuan
 * Date: Nov 25, 2013
 * 深圳快播科技
 */
package com.imtech.imshare.sns.auth;

/**
 * @author Xiaoyuan
 *
 */
public abstract class AuthBase implements IAuth{

	protected IAuthListener mListener;
	
	@Override
	public void setListener(IAuthListener l) {
		mListener = l;
	};
	
}
