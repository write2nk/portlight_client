package com.sirachlabs.portchlyt_services;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.flutterwave.raveandroid.RaveConstants;
import com.flutterwave.raveandroid.RavePayActivity;
import com.flutterwave.raveandroid.RavePayManager;
import com.flutterwave.raveandroid.responses.SubAccount;
import com.koushikdutta.ion.Ion;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import MainActivityTabs.SearchServicesFragment;
import globals.globals;
import models.mClient;
import models.mJobs.mJobs;

public class CardPaymentActivity extends AppCompatActivity {


    String _job_id;
    String tag = "CardPaymentActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_payment);

        _job_id = getIntent().getStringExtra("_job_id");

        mJobs job = app.db.mJobsDao().get_job(_job_id);
        Double amount = job.get_the_total_price();

        //create artisans sub account
        List<SubAccount> artisan_sub_accounts = new ArrayList<>();
        try {

            SubAccount artisan_sub_account =
                    new SubAccount(job.subaccount_id,
                            globals.rave_flutter_wave_split_ratio
                            );
            artisan_sub_accounts.add(artisan_sub_account);
            //Toast.makeText(this,job.subaccount_id,Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(tag, ex.getMessage());
        }

        mClient client = app.db.mClientDao().get_client();

        String narration = "artisan_payment";
        String currency = "NGN";
        String country = "NG";
        String txRef = narration + "_"+job.description + "_" + UUID.randomUUID().toString();
        String publicKey = globals.rave_flutter_wave_public_key;
        String encryptionKey = globals.rave_flutter_wave_encryption_key;
        String email = "email@email.com";
        String fName = "client:"+client.mobile;
        String lName = "artisan:"+job.artisan_mobile;


        try {

            new RavePayManager(CardPaymentActivity.this)
                    .setAmount(amount)
                    .setCountry(country)
                    .setCurrency(currency)
                    .setEmail(email)
                    .setfName(fName)
                    .setlName(lName)
                    .setNarration(narration)
                    .setPublicKey(publicKey)
                    .setEncryptionKey(encryptionKey)
                    .setTxRef(txRef)
                    .acceptAccountPayments(true)
                    .acceptCardPayments(true)
                    .acceptMpesaPayments(false)
                    .acceptAchPayments(false)
                    .acceptGHMobileMoneyPayments(false)
                    .acceptUgMobileMoneyPayments(false)
                    .onStagingEnv(false)
                    //.allowSaveCardFeature(false)
                    //.setMeta(List < Meta >)
                    .withTheme(R.style.RaveFlutterWave)
                    .isPreAuth(false)//must be false no pre-authing is needed
                    .setSubAccounts(artisan_sub_accounts)
                    .shouldDisplayFee(true)
                    .initialize();
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(tag, ex.getMessage());
        }


    }//.oncreate


    //run this on success
    private void run() {
        ProgressDialog pd = new ProgressDialog(CardPaymentActivity.this);
        pd.setMessage(getString(R.string.please_wait));
        pd.show();
        mJobs job = app.db.mJobsDao().get_job(_job_id);
        String artisan_app_id = job.artisan_app_id;
        mClient client = app.db.mClientDao().get_client();
        Ion.with(CardPaymentActivity.this)
                .load(globals.base_url + "/make_payment_for_artisan")
                .setBodyParameter("_job_id", job._job_id)
                .setBodyParameter("client_app_id", client.app_id)
                .setBodyParameter("artisan_app_id", job.artisan_app_id)
                .setBodyParameter("amount_payed", job.get_the_total_price() + "")
                .setBodyParameter("payment_type", "card")
                .asString()
                .setCallback((e, result) -> {
                    pd.dismiss();
                    if (e == null) {
                        try {
                            JSONObject json = new JSONObject(result);
                            String res = json.getString("res");
                            String msg = json.getString("msg");
                            if (res.equals("ok")) {
                                Toast.makeText(CardPaymentActivity.this, getString(R.string.payment_recieved), Toast.LENGTH_SHORT).show();
                                //remove icon and let the plain icon come up
                                SearchServicesFragment.remove_selected_artisan_icon(artisan_app_id);
                                finish();
                            } else {
                                Toast.makeText(CardPaymentActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                                Log.e(tag, msg);
                                finish();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(CardPaymentActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                            Log.e(tag, ex.getMessage());
                            finish();
                        }
                    } else {
                        Toast.makeText(CardPaymentActivity.this, getString(R.string.error_occured), Toast.LENGTH_SHORT).show();
                        Log.e(tag, "line 121");
                        finish();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RaveConstants.RAVE_REQUEST_CODE && data != null) {
            String message = data.getStringExtra("response");
            if (resultCode == RavePayActivity.RESULT_SUCCESS) {
                Toast.makeText(this, getString(R.string.success), Toast.LENGTH_SHORT).show();
                //send to server
                run();
            } else if (resultCode == RavePayActivity.RESULT_ERROR) {
                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                finish();
            } else if (resultCode == RavePayActivity.RESULT_CANCELLED) {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
            finish();
        }
    }


}
