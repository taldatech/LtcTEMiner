package com.example.android.ltcteminer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.prototypes.CardWithList;
import it.gmariotti.cardslib.library.prototypes.LinearListView;

import static android.support.v7.widget.AppCompatDrawableManager.get;

/**
 * Created by Tal on 30/07/2017.
 */

public class ExchangeRatesCard extends CardWithList {

    public static final String dataUrl ="http://ltc.blockr.io/api/v1/exchangerate/current";
    private getRatesAsyncTask mGetRatesTask;
    private double mAmount = 1.0;
    private boolean getError = false;

    public ExchangeRatesCard(Context context) {
        super(context);
    }

    @Override
    protected CardHeader initCardHeader() {
        CardHeader header = new CardHeader(getContext());
        header.setPopupMenu(R.menu.card_rates_menu_item, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.change_amount_action:
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.enter_amount_dialog,null);
                        final EditText etAmount = (EditText) alertLayout.findViewById(R.id.et_amount);
                        etAmount.setText(R.string.enter_amount_default);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.enter_amount_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Not setting amount...",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mAmount = Double.parseDouble(etAmount.getText().toString());
                                Toast.makeText(getContext(),"Amount Set to: " + mAmount ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        init();
                        break;
                }
            }
        });
        header.setTitle(getContext().getString(R.string.card_rates_header_title));
        return header;
    }

    @Override
    protected void initCard() {
        setSwipeable(false);
        setUseProgressBar(true);
    }

    @Override
    protected List<ListObject> initChildren() {
        mGetRatesTask = new getRatesAsyncTask();
        mGetRatesTask.execute(dataUrl);
        ArrayList<Double> ratesArray = new ArrayList<Double>();
        try {
            ratesArray =  mGetRatesTask.get();
        } catch (InterruptedException e) {
            getError = true;
            e.printStackTrace();
        } catch (ExecutionException e) {
            getError = true;
            e.printStackTrace();
        }
        List<ListObject> mObjects = new ArrayList<ListObject>();

        int networkStat = NetworkUtil.getConnectivityStatus(getContext());
        if (networkStat == NetworkUtil.TYPE_NOT_CONNECTED || getError || ratesArray.isEmpty()) {
            RatesObject noInternet = new RatesObject(this);
            noInternet.coin = getContext().getString(R.string.no_internet_coin);
            noInternet.amount = 0;
            noInternet.rate = 0;
            noInternet.currencyIcon = android.R.drawable.stat_notify_error;
            noInternet.currencyUnits = getContext().getString(R.string.no_internet_units);
            mObjects.add(noInternet);

        } else {
            RatesObject btc = new RatesObject(this);
            btc.coin = getContext().getString(R.string.bitcoin);
            btc.amount = mAmount;
            btc.rate = round(ratesArray.get(0),3);
            btc.currencyIcon = R.drawable.btc_icon_scaled;
            btc.currencyUnits = getContext().getString(R.string.bitcoin_units);
            mObjects.add(btc);

            RatesObject usd = new RatesObject(this);
            usd.coin = getContext().getString(R.string.usd);
            usd.amount = mAmount;
            usd.rate = round(ratesArray.get(1),3);
            usd.currencyIcon = R.drawable.usd_icon_scaled;
            usd.currencyUnits = getContext().getString(R.string.usd_units);
            mObjects.add(usd);

            RatesObject ils = new RatesObject(this);
            ils.coin = getContext().getString(R.string.ils);
            ils.amount = mAmount;
            ils.rate = round(ratesArray.get(2),3);
            ils.currencyIcon = R.drawable.ils_icon_scaled;
            ils.currencyUnits = getContext().getString(R.string.ils_units);
            mObjects.add(ils);
        }

        return mObjects;
    }

    @Override
    public View setupChildView(int childPosition, ListObject object, View convertView, ViewGroup parent) {
        TextView currency = (TextView) convertView.findViewById(R.id.card_rates_currency);
        ImageView icon = (ImageView) convertView.findViewById(R.id.card_rates_item_icon);
        TextView amount = (TextView) convertView.findViewById(R.id.card_rates_amount);
        TextView units = (TextView) convertView.findViewById(R.id.card_rates_units);

        //Retrieve the values from the object
        RatesObject ratesObject= (RatesObject)object;
        currency.setText(ratesObject.coin);
        icon.setImageResource(ratesObject.currencyIcon);
        String rateS = "" + ratesObject.rate;
        amount.setText(rateS);
        units.setText(ratesObject.currencyUnits);

        return  convertView;
    }

    @Override
    public int getChildLayoutId() {
        return R.layout.card_rates_inner_main;
    }

    //Rates Object

    public class RatesObject extends CardWithList.DefaultListObject {
        public String coin;
        public double rate;
        public double amount;
        public int currencyIcon;
        public String currencyUnits;

        public RatesObject(Card parentCard) {
            super(parentCard);
            init();
        }

        public void init() {
            //OnClickListener
            setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(LinearListView parent, View view, int position, ListObject object) {
                    String text = mAmount + " LTC = " + round(mAmount*rate,4) + " " + currencyUnits;
                    Toast.makeText(getContext(),text,Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    public static double round (double value, int places) {
        if (places<0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private class getRatesAsyncTask extends AsyncTask<String,Void,ArrayList<Double>> {

        @Override
        protected ArrayList<Double> doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            ArrayList<Double> result = QueryUtilsRates.extractRates(urls[0]);
            return result;
        }
    }
}




