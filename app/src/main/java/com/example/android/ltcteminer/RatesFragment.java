package com.example.android.ltcteminer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.CardThumbnail;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardViewNative;

import static android.view.View.GONE;


public class RatesFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    int tabNumber;
    private getBalanceAsyncTask mGetBalanceTask;

//    private OnFragmentInteractionListener mListener;

    public RatesFragment() {
        // Required empty public constructor
    }

    public static RatesFragment newInstance(int currentTab) {
        RatesFragment fragment = new RatesFragment();
        Bundle args = new Bundle();
        String param1 = "" + currentTab;
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getArguments() != null) {
            tabNumber = Integer.parseInt(getArguments().getString(ARG_PARAM1));
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rates, container, false);
//        final View fView = view;
//        //Card Views
//        ExchangeRatesCard card = new ExchangeRatesCard(getActivity());
//        CardViewNative cardView = (CardViewNative) view.findViewById(R.id.ltc_card);
//        card.init();
////        card.initCard();
////        card.initCardHeader();
////        card.initChildren();
//
//        cardView.setCard(card);
//
//        //Create a Card
//        Card walletCard = new Card(getActivity());
//
//        //Create thumbnail
//        CardThumbnail thumb = new CardThumbnail(getActivity());
//
//        //Set ID resource
//        thumb.setDrawableResource(R.drawable.ltc_icon);
//
//        //Add thumbnail to a card
//        walletCard.addCardThumbnail(thumb);
//
//        //Set card in the cardView
//        CardViewNative cardViewWallet = (CardViewNative) view.findViewById(R.id.wallet_card);
//       // walletCard.setTitle("Wallet Card");
//        CardHeader header = new CardHeader(getContext());
//        header.setPopupMenu(R.menu.card_wallet_menu_item, new CardHeader.OnClickCardHeaderPopupMenuListener() {
//            @Override
//            public void onMenuItemClick(BaseCard card, MenuItem item) {
//                switch (item.getItemId()) {
//                    case R.id.refresh_action:
//                        setBalance(fView);
////                        LayoutInflater inflater = LayoutInflater.from(getContext());
////                        View alertLayout = inflater.inflate(R.layout.enter_amount_dialog,null);
////                        final EditText etAmount = (EditText) alertLayout.findViewById(R.id.et_amount);
////                        etAmount.setText(R.string.enter_amount_default);
////                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
////                        alert.setTitle(getContext().getString(R.string.enter_amount_title));
////                        alert.setView(alertLayout);
////                        alert.setCancelable(false);
////                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
////                            @Override
////                            public void onClick(DialogInterface dialog, int which) {
////                                Toast.makeText(getContext(),"Not setting amount...",Toast.LENGTH_SHORT).show();
////                            }
////                        });
////                        alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
////                            @Override
////                            public void onClick(DialogInterface dialog, int which) {
////                                mAmount = Double.parseDouble(etAmount.getText().toString());
////                                Toast.makeText(getContext(),"Amount Set to: " + mAmount ,Toast.LENGTH_SHORT).show();
////                            }
////                        });
////                        AlertDialog dialog = alert.create();
////                        dialog.show();
//                        break;
//                }
//            }
//        });
//        header.setTitle(getContext().getString(R.string.card_wallet_header_title));
//        walletCard.addCardHeader(header);
//        cardViewWallet.setCard(walletCard);
//
//        //Get info from the internet:
//        setBalance(view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View fView = getView();
        //Card Views
        ExchangeRatesCard card = new ExchangeRatesCard(getActivity());
        CardViewNative cardView = (CardViewNative) fView.findViewById(R.id.ltc_card);
        card.init();

        cardView.setCard(card);

        //Create a Card
        Card walletCard = new Card(getActivity());

        //Create thumbnail
        CardThumbnail thumb = new CardThumbnail(getActivity());

        //Set ID resource
        thumb.setDrawableResource(R.drawable.ltc_icon);

        //Add thumbnail to a card
        walletCard.addCardThumbnail(thumb);

        //Set card in the cardView
        CardViewNative cardViewWallet = (CardViewNative) fView.findViewById(R.id.wallet_card);
        // walletCard.setTitle("Wallet Card");
        CardHeader header = new CardHeader(getContext());
        header.setPopupMenu(R.menu.card_wallet_menu_item, new CardHeader.OnClickCardHeaderPopupMenuListener() {
            @Override
            public void onMenuItemClick(BaseCard card, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.refresh_action:
                        setBalance(fView);
                        break;
                }
            }
        });
        header.setTitle(getContext().getString(R.string.card_wallet_header_title));
        walletCard.addCardHeader(header);
        cardViewWallet.setCard(walletCard);

        //Get info from the internet:
        setBalance(fView);
    }

    private class getBalanceAsyncTask extends AsyncTask<String,Void,Double> {

        @Override
        protected Double doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }
            Double result = QueryUtilsWallet.extractBalance(urls[0]);
            return result;
        }
    }

    void setBalance(View view) {
        double balance;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String mWallet = prefs.getString((getString(R.string.settings_wallet_key)),getString(R.string.settings_wallet_default));
        TextView walletBalacnceTv = (TextView) view.findViewById(R.id.wallet_balance_text_view);
        TextView unitTv = (TextView) view.findViewById(R.id.unit_balance_text_view);
        int networkStat = NetworkUtil.getConnectivityStatus(getActivity());
        if (networkStat == NetworkUtil.TYPE_NOT_CONNECTED) {
            walletBalacnceTv.setText("No Internet Connection");
            unitTv.setVisibility(GONE);
        }
        else if (mWallet.equals(getString(R.string.settings_wallet_default))) {
            unitTv.setVisibility(GONE);
            walletBalacnceTv.setText("Enter Wallet ID in Settings");
        }
        else {
            String requestUrl = "https://chainz.cryptoid.info/ltc/api.dws?q=getbalance&a=" + mWallet;
            mGetBalanceTask = new getBalanceAsyncTask();
            try {
                balance = mGetBalanceTask.execute(requestUrl).get();
                walletBalacnceTv.setText(""+balance);
                unitTv.setVisibility(View.VISIBLE);
            } catch (InterruptedException e) {
                walletBalacnceTv.setText("No Internet Connection");
                unitTv.setVisibility(GONE);
                e.printStackTrace();
            } catch (ExecutionException e) {
                walletBalacnceTv.setText("No Internet Connection");
                unitTv.setVisibility(GONE);
                e.printStackTrace();
            }
        }

    }

//    // TODO: Rename method, update argument and hook method into UI event
//    public void onButtonPressed(Uri uri) {
//        if (mListener != null) {
//            mListener.onFragmentInteraction(uri);
//        }
//    }
//
//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }
//
//    /**
//     * This interface must be implemented by activities that contain this
//     * fragment to allow an interaction in this fragment to be communicated
//     * to the activity and potentially other fragments contained in that
//     * activity.
//     * <p>
//     * See the Android Training lesson <a href=
//     * "http://developer.android.com/training/basics/fragments/communicating.html"
//     * >Communicating with Other Fragments</a> for more information.
//     */
//    public interface OnFragmentInteractionListener {
//        // TODO: Update argument type and name
//        void onFragmentInteraction(Uri uri);
//    }
}
