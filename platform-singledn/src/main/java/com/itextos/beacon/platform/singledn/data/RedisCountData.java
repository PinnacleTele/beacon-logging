// package com.itextos.beacon.platform.singledn.data;
//
// import com.itextos.beacon.commonlib.utility.CommonUtility;
//
// import redis.clients.jedis.Response;
//
// public class RedisCountData
// {
//
// private final boolean mIsSuccess;
// private final int mTotalCount;
// private final Response<Long> mTotalReceivedCount;
// private final Response<Long> mSuccessCount;
// private final Response<Long> mFailureCount;
// private final Response<String> mStrSuccessCount;
// private final Response<String> mStrFailureCount;
//
// private int totalReceivedCount = -1;
// private int successCount = -1;
// private int failureCount = -1;
//
// public RedisCountData(
// boolean aIsSuccess,
// int aTotalCount,
// Response<Long> aTotalReceivedCount,
// Response<Long> aSuccessCount,
// Response<Long> aFailureCount,
// Response<String> aStrSuccessCount,
// Response<String> aStrFailureCount)
// {
// super();
// mIsSuccess = aIsSuccess;
// mTotalCount = aTotalCount;
// mTotalReceivedCount = aTotalReceivedCount;
// mSuccessCount = aSuccessCount;
// mFailureCount = aFailureCount;
// mStrSuccessCount = aStrSuccessCount;
// mStrFailureCount = aStrFailureCount;
//
// process();
// }
//
// public boolean isFirstInsert()
// {
// return totalReceivedCount == mTotalCount;
// }
//
// public boolean isAllPartsReceived()
// {
// return mTotalCount == totalReceivedCount;
// }
//
// private void process()
// {
// Long tempLong = mTotalReceivedCount.get();
// totalReceivedCount = tempLong != null ? tempLong.intValue() : -1;
//
// if (mIsSuccess)
// {
// tempLong = mSuccessCount.get();
// successCount = tempLong != null ? tempLong.intValue() : -1;
// failureCount = CommonUtility.getInteger(mStrFailureCount.get(), -1);
// }
// else
// {
// tempLong = mFailureCount.get();
// successCount = CommonUtility.getInteger(mStrSuccessCount.get(), -1);
// failureCount = tempLong != null ? tempLong.intValue() : -1;
// }
// }
//
// @Override
// public String toString()
// {
// return "RedisCountData [mIsSuccess=" + mIsSuccess + ", mTotalCount=" +
// mTotalCount + ", mTotalReceivedCount=" + mTotalReceivedCount + ",
// mSuccessCount=" + mSuccessCount + ", mFailureCount="
// + mFailureCount + ", mStrSuccessCount=" + mStrSuccessCount + ",
// mStrFailureCount=" + mStrFailureCount + ", totalReceivedCount=" +
// totalReceivedCount + ", successCount=" + successCount
// + ", failureCount=" + failureCount + "]";
// }
//
// }
