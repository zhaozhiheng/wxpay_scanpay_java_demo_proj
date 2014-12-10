package com.tencent.business;

import com.tencent.BeforeTest;
import com.tencent.bridge.IBridge;
import com.tencent.business.mockservice.MockReverseService;
import com.tencent.business.mockservice.MockScanPayQueryService;
import com.tencent.business.mockservice.MockScanPayService;
import com.tencent.common.Configure;
import com.tencent.listener.DefaultScanPayBusinessResultListener;
import com.tencent.protocol.pay_protocol.ScanPayReqData;
import com.tencent.business.bridgefortest.BridgeForScanPayBusinessTest;

import static org.junit.Assert.*;

import com.tencent.service.ScanPayService;
import org.junit.*;
import com.tencent.business.bridgefortest.BridgeForScanPayBusinessCase2Test;
import com.tencent.business.bridgefortest.BridgeForScanPayBusinessCase5Test;

/**
 * ScanPayBusinessDemo Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>ʮһ�� 4, 2014</pre>
 */
public class ScanPayBusinessTest {

    private static ScanPayBusiness scanPayBusiness;
    
    //普通的Bridge，主要用来测试用本地xml数据模拟API服务器返回的情况
    private static BridgeForScanPayBusinessTest bridgeForScanPayBusinessTest;

    //这个IBridge类专门用来模拟参数没传对的情况，测试ScanPayBusinessDemo中的Case2
    private static BridgeForScanPayBusinessCase2Test bridgeForScanPayBusinessCase2Test;

    //这个IBridge类专门用来模拟authCode不合法的情况，测试ScanPayBusinessDemo中的Case5
    private static BridgeForScanPayBusinessCase5Test bridgeForScanPayBusinessCase5Test;

    private static DefaultScanPayBusinessResultListener resultListener;

    @BeforeClass
    public static void beforeClass() throws IllegalAccessException, InstantiationException, ClassNotFoundException {

        BeforeTest.initSDK();
        //自定义底层的HttpsRequest
        Configure.setHttpsRequestClassName("com.tencent.httpsrequest.HttpsRequestForTest");

        scanPayBusiness = new ScanPayBusiness();
        bridgeForScanPayBusinessTest = new BridgeForScanPayBusinessTest();
        bridgeForScanPayBusinessCase2Test = new BridgeForScanPayBusinessCase2Test();
        bridgeForScanPayBusinessCase5Test = new BridgeForScanPayBusinessCase5Test();
        resultListener = new DefaultScanPayBusinessResultListener();
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
        //恢复正常的服务
        scanPayBusiness.setScanPayService(new ScanPayService());
        resultListener.setResult("");
    }

    private void runBusiness(IBridge bridge, ScanPayBusiness.ResultListener listener) throws Exception {

        ScanPayReqData scanPayReqData= new ScanPayReqData(
                bridge.getAuthCode(),
                bridge.getBody(),
                bridge.getAttach(),
                bridge.getOutTradeNo(),
                bridge.getTotalFee(),
                bridge.getDeviceInfo(),
                bridge.getSpBillCreateIP(),
                bridge.getTimeStart(),
                bridge.getTimeExpire(),
                bridge.getGoodsTag()
        );

        scanPayBusiness.run(
                scanPayReqData,
                listener
        );
    }

    /**
     * Method: run()
     */
    @Ignore
    public void testRun() throws Exception {
//TODO: Test goes here...


    }

    /**
     * 测试Case1
     * 通过本地XML数据模拟API后台返回数据没内容的情况
     *
     * @throws Exception
     */
    @Test
    public void testCase1() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/apierror.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_RETURN_CODE_ERROR);
    }

    /**
     * 测试Case2
     * 通过本地XML数据模拟API后台返回参数不合法的情况
     *
     * @throws Exception
     */
    @Test
    public void testCase2() throws Exception {
        runBusiness(bridgeForScanPayBusinessCase2Test, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_RETURN_CODE_FAIL);
    }

    /**
     * 测试Case3
     * 通过本地XML数据模拟API后台返回的数据被篡改的情况
     *
     * @throws Exception
     */
    @Test
    public void testCase3() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/hackdata.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_SIGN_INVALID);
    }

    /**
     * 测试Case4
     * 这个基本不需要通过本地数据模拟了，只要run一下scanPayBusiness.run()就是这个结果了，因为如果没有手动去更新auth_code的话，他基本处于过期状态（有效期才一分钟）
     *
     * @throws Exception
     */
    @Test
    public void testCase4() throws Exception {
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_AUTH_CODE_EXPIRE);
    }

    /**
     * 测试Case5
     * 这个基本不需要通过本地数据模拟了，只要run一下scanPayBusiness.run()就是这个结果了，因为如果没有手动去更新auth_code的话，他基本处于过期状态（有效期才一分钟）
     *
     * @throws Exception
     */
    @Test
    public void testCase5() throws Exception {
        runBusiness(bridgeForScanPayBusinessCase5Test, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_AUTH_CODE_INVALID);
    }

    /**
     * 测试Case6
     * 这个通过本地模拟API返回数据来测试“Case6:【支付失败】原因是：用户余额不足，换其他卡支付或是用现金支付”分支
     *
     * @throws Exception
     */
    @Test
    public void testCase6() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/notenough.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL_BY_MONEY_NOT_ENOUGH);
    }


    /**
     * 测试Case7
     * 这个通过本地模拟API返回数据来测试“Case7:【查询到支付成功】”分支
     *
     * @throws Exception
     */
    @Test
    public void testCase7() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/usepaying.xml"));
        scanPayBusiness.setScanPayQueryService(new MockScanPayQueryService("/payqueryserviceresponsedata/payquerysuccess.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_SUCCESS);
    }

    /**
     * 测试Case8
     * 这个通过本地模拟API返回数据来测试“Case8:【撤销成功】”分支
     *
     * @throws Exception
     */
    @Test
    public void testCase8() throws Exception {
        //自定义调用查询接口的间隔
        scanPayBusiness.setWaitingTimeBeforePayQueryServiceInvoked(3000);
        //自定义调用查询接口的次数
        scanPayBusiness.setPayQueryLoopInvokedCount(1);
        //自定义调用撤销接口的间隔
        scanPayBusiness.setWaitingTimeBeforeReverseServiceInvoked(2000);

        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/usepaying.xml"));
        scanPayBusiness.setScanPayQueryService(new MockScanPayQueryService("/payqueryserviceresponsedata/payqueryfail.xml"));
        scanPayBusiness.setReverseService(new MockReverseService("/reverseserviceresponsedata/reversesuccess.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL);
    }


    /**
     * 测试Case10
     * 这个通过本地模拟API返回数据来测试“Case10:【支付成功】”分支
     *
     * @throws Exception
     */
    @Test
    public void testCase10() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/paysuccess.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_SUCCESS);
    }


    /**
     * 测试Case11
     * 这个通过本地模拟API返回数据来测试“Case11:【支付失败】其他原因”分支
     *
     * @throws Exception
     */
    @Test
    public void testCase11() throws Exception {
        scanPayBusiness.setScanPayService(new MockScanPayService("/payserviceresponsedata/payfail.xml"));
        runBusiness(bridgeForScanPayBusinessTest, resultListener);
        assertEquals(resultListener.getResult(), DefaultScanPayBusinessResultListener.ON_FAIL);
    }


    /**
     * Method: doOnePayQuery(String transactionID, String outTradeNo)
     */
    @Ignore
    public void testDoOnePayQuery() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = scanPayBusiness.getClass().getMethod("doOnePayQuery", String.class, String.class);
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: doPayQueryLoop(int loopCount, String transactionID, String outTradeNo)
     */
    @Ignore
    public void testDoPayQueryLoop() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = scanPayBusiness.getClass().getMethod("doPayQueryLoop", int.class, String.class, String.class);
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: doOneReverse(String transactionID, String outTradeNo)
     */
    @Ignore
    public void testDoOneReverse() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = scanPayBusiness.getClass().getMethod("doOneReverse", String.class, String.class);
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: doReverseLoop(int loopCount, String transactionID, String outTradeNo)
     */
    @Ignore
    public void testDoReverseLoop() throws Exception {
//TODO: Test goes here... 
/* 
try { 
   Method method = scanPayBusiness.getClass().getMethod("doReverseLoop", int.class, String.class, String.class);
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
