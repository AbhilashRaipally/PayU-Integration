package com.example.payucheckout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import com.example.payucheckout.databinding.ActivityMainBinding
import com.payu.base.models.ErrorResponse
import com.payu.base.models.PayUPaymentParams
import com.payu.base.models.PaymentMode
import com.payu.base.models.PaymentType
import com.payu.checkoutpro.PayUCheckoutPro
import com.payu.checkoutpro.models.PayUCheckoutProConfig
import com.payu.checkoutpro.utils.PayUCheckoutProConstants
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_NAME
import com.payu.checkoutpro.utils.PayUCheckoutProConstants.CP_HASH_STRING
import com.payu.ui.model.listeners.PayUCheckoutProListener
import com.payu.ui.model.listeners.PayUHashGenerationListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var payButton: Button

    private val email: String = "snooze@payu.in"
    private val phone = "9999999999"
    private val merchantName = "RH Group"
    private val surl = "https://payuresponse.firebaseapp.com/success"
    private val furl = "https://payuresponse.firebaseapp.com/failure"
    private val amount = "1.0"

    //Test Key and Salt
    private val testKey = "3TnMpV"
    private val testSalt = "g0nGFe03"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        payButton = binding.button

        payButton.setOnClickListener { payClicked(it) }
        setContentView(binding.root)
    }

    private fun payClicked(view: View) {
        val payUPaymentParams = buildPaymentPrams()
        val payUCheckoutProConfig = PayUCheckoutProConfig()

        /*val checkoutOrderList = ArrayList<PaymentMode>()
        checkoutOrderList.add(PaymentMode(PaymentType.UPI, PayUCheckoutProConstants.CP_GOOGLE_PAY))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PHONEPE))
        checkoutOrderList.add(PaymentMode(PaymentType.WALLET, PayUCheckoutProConstants.CP_PAYTM))
        payUCheckoutProConfig.paymentModesOrder = checkoutOrderList*/

        PayUCheckoutPro.open(this, payUPaymentParams, payUCheckoutProConfig, payUCheckoutProListener())
    }

    private fun payUCheckoutProListener() = object : PayUCheckoutProListener {
        override fun onPaymentSuccess(response: Any) {
            response as HashMap<*, *>
            val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
            val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]
        }


        override fun onPaymentFailure(response: Any) {
            response as HashMap<*, *>
            val payUResponse = response[PayUCheckoutProConstants.CP_PAYU_RESPONSE]
            val merchantResponse = response[PayUCheckoutProConstants.CP_MERCHANT_RESPONSE]
        }


        override fun onPaymentCancel(isTxnInitiated: Boolean) {
        }


        override fun onError(errorResponse: ErrorResponse) {
            val errorMessage: String =
                if (errorResponse.errorMessage != null && errorResponse.errorMessage!!.isNotEmpty())
                    errorResponse.errorMessage!!
                else
                    "Unknown Error occurred during payment"
            Log.d("TestApp", errorMessage)
        }

        override fun setWebViewProperties(webView: WebView?, bank: Any?) {
            //For setting webview properties, if any. Check Customized Integration section for more details on this
        }

        //Will be called by SDK to get the hash while making transaction
        override fun generateHash(
            valueMap: HashMap<String, String?>,
            hashGenerationListener: PayUHashGenerationListener
        ) {
            if (valueMap.containsKey(CP_HASH_STRING)
                && valueMap.containsKey(CP_HASH_NAME)) {

                val hashData = valueMap[CP_HASH_STRING]
                val hashName = valueMap[CP_HASH_NAME]

                //Do not generate hash from local,
                // it needs to be calculated from server side only.
                // Here, hashString contains hash created from your server side.
                val hash: String? = HashGenerationUtils.generateHashFromSDK(hashData!!, testSalt)
                if (!TextUtils.isEmpty(hash)) {
                    val dataMap: HashMap<String, String?> = HashMap()
                    dataMap[hashName!!] = hash!!
                    hashGenerationListener.onHashGenerated(dataMap)
                }
            }
        }
    }

    private fun buildPaymentPrams(): PayUPaymentParams {
        return PayUPaymentParams.Builder()
            .setAmount(amount)
            .setIsProduction(true)
            .setKey(testKey)
            .setProductInfo("Test")
            .setPhone(phone)
            .setTransactionId(System.currentTimeMillis().toString()) //Should be unique
            .setFirstName(merchantName)
            .setEmail(email)
            .setSurl(surl) //URL that gets hit when transaction succeeds
            .setFurl(furl) //URL that gets hit when transaction fails
            //.setUserCredential(<String>) //Is used to save card details and fetch them when required
            //.setAdditionalParams(<HashMap<String,Any?>>) //Optional, can contain any additional PG params
            .build()
    }
}