package com.example.android.ltcteminer;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Context.MODE_PRIVATE;
import static android.os.Build.VERSION_CODES.M;


public class SettingsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    int tabNumber;

//    private OnFragmentInteractionListener mListener;

        public SettingsFragment() {
            // Required empty public constructor
        }

        public static SettingsFragment newInstance(int currentTab) {
            SettingsFragment fragment = new SettingsFragment();
            String param1 = "" + currentTab;
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            fragment.setArguments(args);
            return fragment;
        }

        public static class SettingsFragmentInside extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
            @Override
            public void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                addPreferencesFromResource(R.xml.settings_main);

                Preference mWifi = findPreference(getString(R.string.settings_wifi_key));
                bindPreferenceSummaryToValue(mWifi);

                Preference mCharger = findPreference(getString(R.string.settings_charger_key));
                bindPreferenceSummaryToValue(mCharger);

                Preference mWallet = findPreference(getString((R.string.settings_wallet_key)));
                bindPreferenceSummaryToValue((mWallet));

                Preference mPool = findPreference(getString(R.string.settings_pool_key));
                bindPreferenceSummaryToValue(mPool);

                Preference mUser = findPreference(getString(R.string.settings_user_key));
                bindPreferenceSummaryToValue(mUser);

                Preference mPassword = findPreference(getString(R.string.settings_password_key));
                bindPreferenceSummaryToValue(mPassword);


                Preference button = findPreference(getString(R.string.settings_reset_key));
                button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                        prefs.edit().clear().apply();
                        RestartApp();
                        return true;
                    }
                });
            }

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (preference instanceof CheckBoxPreference) {
                    //DO NOTHING
                } else {
                    if (newValue != null) {
                        if ( preference.getTitleRes() != R.string.settings_password_label ) {
                            String stringValue = newValue.toString();
                            preference.setSummary(stringValue);
                        } else {
                            String stringValue = getResources().getString(R.string.settings_password_hidden);
                            preference.setSummary(stringValue);
                        }
                    }
                }
                return true;
            }
            private void bindPreferenceSummaryToValue(Preference preference) {
                if (preference != null) {
                    preference.setOnPreferenceChangeListener(this);
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    if (preference instanceof EditTextPreference) {
                        String preferenceString = preferences.getString(preference.getKey(), "");
                        onPreferenceChange(preference, preferenceString);
                    } else {
                        Boolean boolVal = preferences.getBoolean(preference.getKey(), true);
                        onPreferenceChange(preference,boolVal);
                    }
                }
            }

            public void RestartApp() {
                Intent i = getActivity().getBaseContext().getPackageManager().
                        getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //getActivity().finish();
                startActivity(i);
            }

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            if (getArguments() != null) {
                tabNumber = Integer.parseInt(getArguments().getString(ARG_PARAM1));
            }
            super.onCreate(savedInstanceState);
//            getActivity().getFragmentManager().beginTransaction()
//                    .replace(android.R.id.content, new SettingsFragmentInside())
//                    .commit();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View view = inflater.inflate(R.layout.fragment_settings, container, false);
            return view;
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
