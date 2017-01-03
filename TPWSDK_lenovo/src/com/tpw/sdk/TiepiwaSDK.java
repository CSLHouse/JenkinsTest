package com.tpw.sdk;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.third.SysUtils;
import com.lenovo.lsf.gamesdk.GamePayRequest;
import com.lenovo.lsf.gamesdk.IAuthResult;
import com.lenovo.lsf.gamesdk.IPayResult;
import com.lenovo.lsf.gamesdk.LenovoGameApi;


import com.tpw.sdk.ActiveCode;
import com.tpw.sdk.SdkConstants;
import com.tpw.sdk.SdkListener;


import com.tpw.sdk.TpwSdk;

import android.util.Log;

public class TiepiwaSDK extends TpwSdk
{
	private String CHANNEL_NAME = SdkConstants.LENOVO;
	
	private Map<String, String> mPayParams = null;

	
	private String VERSION = "1.0";
	private String openId = "";
	
	public String getOpenId()
	{		
		return openId;		
	}
	@Override
	public void init()
	{
		super.init();
		
			//SDK初始化
			LenovoGameApi.doInit(mCurrActivity, mAppId);
			Log.d(TAG, "init, success");
			mListener.onCallBack(SdkListener.RET_INIT_SUCCESS, "success");
		
	}
	
	@Override
	public void login()
	{
		super.login();
		LenovoGameApi.doAutoLogin(mCurrActivity, new IAuthResult() {
			
			@Override
			public void onFinished(boolean ret, String data) {
				// TODO Auto-generated method stub
				if (ret) {
					//快速登录成功
					Log.d(TAG, "QuickLogin login success");
					
					mLoginParams.put("version", VERSION);

	        		mLoginParams.put("uid", "");
	        		mLoginParams.put("sessionid", data);
	        		//params.put("sign", SysUtils.getMD5(CHANNEL_NAME + mAppId + mGameId + mGamekey));
	        		doGet(mLoginParams, GET_TYPE_LOGIN);
				} else {
					//后台快速登录失败(失败原因开启飞行模式、 网络不通等)
					Log.i(TAG, "QuickLogin Login Fail");
					mListener.onCallBack(SdkListener.RET_LOGIN_FAIL, "fail");
				}
			}
		});
	}
	
	@Override
	public void exit()
	{
		super.exit();
		Log.d(TAG, "exit");
		LenovoGameApi.doQuit(mCurrActivity, new IAuthResult() {
			
			@Override
			public void onFinished(boolean arg0, String arg1) {
				// TODO Auto-generated method stub
				if(arg0){
					Log.d(TAG, "exit success");
					mListener.onCallBack(SdkListener.RET_EXIT, "success");
				}								
			}
		});
	}
	
	@Override
	public void pay(Map<String, String> params) {
		super.pay(params);
		mPayParams = params;
		
		
		mPayParam.put("version", VERSION);
		mPayParam.put("productid", params.get(PRODUCT_ID));
		mPayParam.put("productcount", params.get(PRODUCT_COUNT));
		mPayParam.put("originalmoney", params.get(PRODUCT_PRICE));
		mPayParam.put("money", params.get(PRODUCT_PRICE));
		mPayParam.put("cpinfo", SysUtils.getBase64Encode(params.get(EXT)));
		doGet(mPayParam, GET_TYPE_ORDER);
	}
	
	@Override
	protected void onLoginResponse(String response) {
		Log.d(TAG, "response:"+response);
		super.onLoginResponse(response);		
//		try {
//			JSONObject json = new JSONObject(response);
//			String code = json.getString("code");
//			if (!code.equals("1")) {
//				mListener.onCallBack(SdkListener.RET_LOGIN_FAIL, "request verify fail");
//			} else {
//				createSession(json.getString("channelid"), mAppId, 
//						json.getString("logintime"), json.getString("sign"));
//				if(boo){
//					Log.d(TAG, "TPWSDK ActiveCode init");
//					ActiveCode.init(mCurrActivity, 
//							CHANNEL_NAME, 
//							mGameId, 
//							mGamekey, 
//							mActiveCodeUrl, 
//							mRequestActiveCodeUrl, 
//							mListener,
//							GET_TYPE_LOGIN);
//					ActiveCode.doActiveCode(mUserID, mChannel);
//					Log.d(TAG, "ActiveCode doActiveCode");
//				} else {
//					Log.d(TAG, "no active code success");
//					mListener.onCallBack(SdkListener.RET_LOGIN_SUCCESS, "success");
//				}								
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}
	
	@Override
	protected void onLoginErrorResponse() {
		super.onLoginErrorResponse();
		//mListener.onCallBack(SdkListener.RET_LOGIN_FAIL, "request verify fail");
	}
	
	@Override
	protected void onOrderResposne(String response) {
		super.onOrderResposne(response);
		
		/***********
		 *  支付LenovoGameApi.doPay（） 接口 调用
		 */
		int pid = 1;
		try
		{
			pid = Integer.parseInt(mPayParams.get(PRODUCT_ID));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		//
		//单位：元
		GamePayRequest payRequest = new GamePayRequest();
		// 请填写商品自己的参数
		payRequest.addParam("notifyurl",  "");
		payRequest.addParam("appid", mAppId);
		//todo:llw  联想的按次购买 
		Log.d(TAG, "pid#= "+pid);
		payRequest.addParam("waresid", pid);
		if(mOrder == null || "".equals(mOrder)){
			return;
		}
		payRequest.addParam("exorderno", mOrder);
		Log.d(TAG, "price#= "+Integer.parseInt(mPayParams.get(PRODUCT_PRICE)));
		payRequest.addParam("price", Integer.parseInt(mPayParams.get(PRODUCT_PRICE)));
		payRequest.addParam("cpprivateinfo", "123456");

		LenovoGameApi.doPay(mCurrActivity, mAppKey, payRequest, new IPayResult() {
			@Override
			public void onPayResult(int resultCode, String signValue,
					String resultInfo) {// resultInfo = 应用编号&商品编号&外部订单号
				if (LenovoGameApi.PAY_SUCCESS == resultCode) {
					Log.e(TAG, "lenovopay signValue = " + signValue);
					Log.e(TAG, "pay success");
					mListener.onCallBack(SdkListener.RET_PAY_SUCCESS, "success");
				} else if (LenovoGameApi.PAY_CANCEL == resultCode) {
//					Toast.makeText(GoodsListActivity.this, "sample:取消支付",
//							Toast.LENGTH_SHORT).show();
					// 取消支付处理，默认采用finish()，请根据需要修改
					Log.e(TAG, "lenovopay return cancel");
					mListener.onCallBack(SdkListener.RET_PAY_FAIL, "fail");
				} else {
//					Toast.makeText(GoodsListActivity.this, "sample:支付失败",
//							Toast.LENGTH_SHORT).show();
					// 计费失败处理，默认采用finish()，请根据需要修改
					Log.e(TAG, "lenovopay return Error");
					mListener.onCallBack(SdkListener.RET_PAY_FAIL, "fail");
				}
			}
		});
	}
	
	@Override
	protected void onOrderErrorResponse() {
		super.onOrderErrorResponse();
		//mListener.onCallBack(SdkListener.RET_PAY_FAIL, "fail");
	}
}
