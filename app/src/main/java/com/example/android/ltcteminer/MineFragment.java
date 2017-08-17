package com.example.android.ltcteminer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.ltcteminer.StratumMiner.MinerActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.R.attr.onClick;
import static android.R.attr.priority;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;
import static com.example.android.ltcteminer.R.string.settings_user_default;
import static com.example.android.ltcteminer.R.string.settings_user_key;


public class MineFragment extends Fragment {

//    private OnFragmentInteractionListener mListener;
    private static final String ARG_PARAM1 = "param1";
    int tabNumber;
    View mView;
    static boolean sparataFlag = false;
    static boolean newUserFlag = false;
    static boolean newPasswordFlag = false;
    static boolean newServerFlag = false;

    SharedPreferences prefs;

    Button b__miner;
    Button b_verify;
    CheckBox cb_run_background;
    CheckBox cb_screen_awake;

    String mUser= "";
    String mPassword = "";
    String mServer = "";


    public MineFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static MineFragment newInstance(int currentTab) {
        MineFragment fragment = new MineFragment();
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
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mine,container,false);
        mView = view;
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setThreads();
        setSpinners();

        b__miner = (Button)getActivity().findViewById(R.id.btn_open_miner);
        //b__miner.setAlpha(.3f);
        b__miner.setClickable(false);
        b__miner.setEnabled(false);

        b_verify = (Button)getActivity().findViewById(R.id.btn_verify);
        b_verify.setEnabled(true);
        b_verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean result = verifySettings();
                if (result) {
                    b__miner.setClickable(true);
                    b__miner.setEnabled(true);
                } else {
                    b__miner.setClickable(false);
                    b__miner.setEnabled(false);
                }
            }
        });

        b__miner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean res = verifySettings();
                if (res) {
                    Toast.makeText(getActivity(), "Access Granted", Toast.LENGTH_LONG).show();
                    b__miner.setClickable(false);
                    b__miner.setEnabled(false);
                    //Prepare Arguments for Miner:
                    Spinner threadsSpinner = (Spinner) getActivity().findViewById(R.id.spinnerCustomThreads);
                    Spinner prioritySpinner = (Spinner) getActivity().findViewById(R.id.spinnerCustomPriority);
                    cb_run_background = (CheckBox) getActivity().findViewById(R.id.settings_checkBox_background);
                    cb_screen_awake = (CheckBox) getActivity().findViewById((R.id.settings_checkBox_screen));
                    int numOfThreads = Integer.parseInt(threadsSpinner.getSelectedItem().toString());
                    Log.i("Open Miner", "numOfThreads= " +numOfThreads);
                    int priorityPos = prioritySpinner.getSelectedItemPosition();
                    int priority = 0;
                    switch (priorityPos) {
                        case 0:
                            priority = Thread.MIN_PRIORITY;
                            break;
                        case 1:
                            priority = Thread.NORM_PRIORITY;
                            break;
                        case 2:
                            priority = Thread.MAX_PRIORITY;
                            break;
                    }
                    Log.i("Open Miner", "Priority= " +priority);
                    boolean run_background = cb_run_background.isChecked();
                    Log.i("Open Miner", "Run On Background= " +run_background);
                    boolean screen_awake = cb_screen_awake.isChecked();
                    Log.i("Open Miner", "Keep Screen Awake= " +screen_awake);
                    //The rest are mServer, mUser, mPassword
                    Intent MineIntent = new Intent(getActivity(), MinerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("server",mServer);
                    bundle.putString("user",mUser);
                    bundle.putString("password",mPassword);
                    bundle.putInt("threads_num",numOfThreads);
                    bundle.putInt("threads_priority",priority);
                    bundle.putBoolean("run_background",run_background);
                    bundle.putBoolean("screen_awake", screen_awake);
                    MineIntent.putExtras(bundle);
                    startActivity(MineIntent);


                } else {
                    Toast.makeText(getActivity(), "Access Denied", Toast.LENGTH_LONG).show();
                    b__miner.setClickable(false);
                    b__miner.setEnabled(false);
                }
            }
        });
    }

    void setThreads()
    {
        try
        {
            //log(Integer.toString(Runtime.getRuntime().availableProcessors()));
            Spinner threadList = (Spinner)getActivity().findViewById(R.id.spinnerCustomThreads);

            ArrayList<String> threadsAv = new ArrayList<String>();

//            String[] threadsAvailable = new String[Runtime.getRuntime().availableProcessors()];

            for(int i = 0; i < Runtime.getRuntime().availableProcessors();i++)
            {
                threadsAv.add(Integer.toString(i + 1));
                //log(Integer.toString(i));
//                threadsAvailable[i] = Integer.toString(i + 1);
//                ArrayAdapter threads = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, threadsAvailable);
//                threadList.setAdapter(threads);
            }
            CustomSpinnerAdapter customSpinnerAdapter=new CustomSpinnerAdapter(getActivity(),threadsAv);
            threadList.setAdapter(customSpinnerAdapter);
        }
        catch (Exception e){}
    }

    void setSpinners() {
        Spinner prioritydList = (Spinner)getActivity().findViewById(R.id.spinnerCustomPriority);
        final Spinner userList = (Spinner)getActivity().findViewById(R.id.spinnerCustomUser);
        final Spinner passwordList = (Spinner)getActivity().findViewById(R.id.spinnerCustomPassword);
        final Spinner serverList = (Spinner)getActivity().findViewById(R.id.spinnerCustomServer);

        final ArrayList<String> priorities = new ArrayList<String>();
        ArrayList<String> users = new ArrayList<String>();
        ArrayList<String> passwords = new ArrayList<String>();
        ArrayList<String> servers = new ArrayList<String>();

        //Priorities
        priorities.add(getString(R.string.settings_min_priority));
        priorities.add(getString(R.string.settings_norm_priority));
        priorities.add(getString(R.string.settings_max_priority));


        CustomSpinnerAdapter prioritiesAdapter=new CustomSpinnerAdapter(getActivity(),priorities);
        prioritydList.setAdapter(prioritiesAdapter);
        prioritydList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    sparataFlag = false;
                }
                if (position == 1) {
                    sparataFlag = false;
                }
                if (position == priorities.size()-1 && sparataFlag == false) {
                    sparataFlag = true;
                    Toast.makeText(parent.getContext(), "THIS IS SPARTA!", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
             //NOTHING
            }
        });

        //Users:
        final TextView userView = (TextView)getActivity().findViewById(R.id.user_view);
        int userNum;
        users.add(getString(R.string.settings_pick_user));
        users.add(getString(R.string.settings_wallet_as_user));
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        final String defaultWallet = prefs.getString((getString(R.string.settings_wallet_key)),getString(R.string.settings_wallet_default));
        final String defaultUser = prefs.getString(getString(R.string.settings_user_key),getString(R.string.settings_user_default));
        if (defaultUser.equals(getString(R.string.settings_user_default))) {
            users.add(getString(R.string.settings_new_user));
            userNum = 3;
        } else {
            users.add(defaultUser);
            users.add(getString(R.string.settings_new_user));
            userNum = 4;
        }
        CustomSpinnerAdapter usersAdapter=new CustomSpinnerAdapter(getActivity(),users);
        userList.setAdapter(usersAdapter);
        final int finalUserNum = userNum;
        userList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (finalUserNum == 3) {
                    if (position == 0) {
                        newUserFlag = false;
                    }
                    if (position == 1) {
                        newUserFlag = false;
                        if (defaultWallet.equals(getString(R.string.settings_wallet_default))) {
                            //Wallet not set
                            mUser = "";
                            userView.setText("Wallet ID not found!");
                            userList.setSelection(0);
                        } else {
                            mUser = defaultWallet;
                            userView.setText("User: using Wallet ID");
                        }
                    }
                    if (position == 2  && newUserFlag == false) {
                        //New User
                        Toast.makeText(getActivity(),"New user", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_user_dialog,null);
                        final EditText etUser = (EditText) alertLayout.findViewById(R.id.et_user);
                        //etUser.setText(R.string.settings_new_user_enter);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_user_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUser = etUser.getText().toString();
                                userView.setText("User: " + mUser);
                                Toast.makeText(getContext(),"New User: " + mUser + " Saved" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        newUserFlag = true;
                        userList.setSelection(0);
                    }
                }
                else if (finalUserNum == 4) {
                    if (position == 0) {
                        newUserFlag = false;
                    }
                    if (position == 1) {
                        newUserFlag = false;
                        if (defaultWallet.equals(getString(R.string.settings_wallet_default))) {
                            //Wallet not set
                            mUser = "";
                            userView.setText("Wallet ID not found!");
                            userList.setSelection(0);
                        } else {
                            mUser = defaultWallet;
                            userView.setText("User: using Wallet ID");
                        }
                    }
                    if (position == 2) {
                        //Default User
                        //Toast.makeText(getActivity(),"default user", Toast.LENGTH_SHORT).show();
                        mUser = defaultUser;
                        userView.setText("User: " + mUser);
                        newUserFlag = false;
                    }
                    if (position == 3 && newUserFlag == false) {
                        newUserFlag = true;
                        Toast.makeText(getActivity(),"New user", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_user_dialog,null);
                        final EditText etUser = (EditText) alertLayout.findViewById(R.id.et_user);
                        //etUser.setText(R.string.settings_new_user_enter);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_user_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_LONG).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mUser = etUser.getText().toString();
                                userView.setText("User: " + mUser);
                                Toast.makeText(getContext(),"New User: " + mUser + " Saved" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        userList.setSelection(0);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //NOTHING
            }
        });

        //Passwords:
        final TextView passView = (TextView)getActivity().findViewById(R.id.password_view);
        int passNum;
        passwords.add(getString(R.string.settings_pick_pass));
        final String defaultPass = prefs.getString(getString(R.string.settings_password_key),getString(R.string.settings_password_default));
        if (defaultPass.equals(getString(R.string.settings_password_default))) {
            passwords.add(getString(R.string.settings_new_pass));
            passNum = 2;
        } else {
            passwords.add(getString(R.string.use_default_pass));
            passwords.add(getString(R.string.settings_new_pass));
            passNum = 3;
        }
        CustomSpinnerAdapter passAdapter=new CustomSpinnerAdapter(getActivity(),passwords);
        passwordList.setAdapter(passAdapter);
        final int finalPassNum = passNum;
        passwordList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (finalPassNum == 2) {
                    if (position == 0) {
                        newPasswordFlag = false;
                    }
                    if (position == 1  && newPasswordFlag == false) {
                        //New Password
                        Toast.makeText(getActivity(),"New password", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_password_dialog,null);
                        final EditText etPass = (EditText) alertLayout.findViewById(R.id.et_pass);
                        //etUser.setText(R.string.settings_new_user_enter);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_pass_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPassword = etPass.getText().toString();
                                passView.setText("Password: using New Password");
                                Toast.makeText(getContext(),"New Password Saved" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        newPasswordFlag = true;
                        passwordList.setSelection(0);
                    }
                }
                else if (finalPassNum == 3) {
                    if (position == 0) {
                        newPasswordFlag = false;
                    }
                    if (position == 1) {
                        //Default Password
                        //Toast.makeText(getActivity(),"default user", Toast.LENGTH_SHORT).show();
                        mPassword = defaultPass;
                        passView.setText("Password: using Default Password");
                        newPasswordFlag = false;
                    }
                    if (position == 2 && newPasswordFlag == false) {
                        //New Password
                        Toast.makeText(getActivity(),"New password", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_password_dialog,null);
                        final EditText etPass = (EditText) alertLayout.findViewById(R.id.et_pass);
                        //etUser.setText(R.string.settings_new_user_enter);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_pass_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mPassword = etPass.getText().toString();
                                passView.setText("Password: using New Password");
                                Toast.makeText(getContext(),"New Password Saved" ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        newPasswordFlag = true;
                        passwordList.setSelection(0);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //NOTHING
            }
        });

        //Server:
        final TextView urlView = (TextView)getActivity().findViewById(R.id.pool_url_selected);
        int serverNum;
        servers.add(getString(R.string.settings_pick_url));
        final String defaultUrl = prefs.getString(getString(R.string.settings_pool_key),getString(R.string.settings_pool_default));
        if (defaultUrl.equals(getString(R.string.settings_pool_default))) {
            servers.add(getString(R.string.settings_new_url));
            serverNum = 2;
        } else {
            servers.add(getString(R.string.use_default_url));
            servers.add(getString(R.string.settings_new_url));
            serverNum = 3;
        }
        CustomSpinnerAdapter serverAdapter=new CustomSpinnerAdapter(getActivity(),servers);
        serverList.setAdapter(serverAdapter);
        final int finalServerNum = serverNum;
        serverList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (finalServerNum == 2) {
                    if (position == 0) {
                        newServerFlag = false;
                    }
                    if (position == 1  && newServerFlag == false) {
                        //New Server
                        Toast.makeText(getActivity(),"New server", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_url_dialog,null);
                        final EditText etUrl = (EditText) alertLayout.findViewById(R.id.et_url);
//                        etUrl.setText(R.string.pool_url_format);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_url_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mServer = etUrl.getText().toString();
                                urlView.setText("Pool URL: " + mServer);
                                Toast.makeText(getContext(),"" + mServer ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        newServerFlag = true;
                        serverList.setSelection(0);
                    }
                }
                else if (finalServerNum == 3) {
                    if (position == 0) {
                        newServerFlag = false;
                    }
                    if (position == 1) {
                        //Default Server
                        //Toast.makeText(getActivity(),"default user", Toast.LENGTH_SHORT).show();
                        mServer = defaultUrl;
                        urlView.setText("" + mServer);
                        newServerFlag = false;
                    }
                    if (position == 2 && newServerFlag == false) {
                        //New Server
                        Toast.makeText(getActivity(),"New server", Toast.LENGTH_SHORT).show();
                        LayoutInflater inflater = LayoutInflater.from(getContext());
                        View alertLayout = inflater.inflate(R.layout.new_url_dialog,null);
                        final EditText etUrl = (EditText) alertLayout.findViewById(R.id.et_url);
//                        etUrl.setText(R.string.pool_url_format);
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle(getContext().getString(R.string.settings_new_url_enter_title));
                        alert.setView(alertLayout);
                        alert.setCancelable(false);
                        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getContext(),"Canceled",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alert.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mServer = etUrl.getText().toString();
                                urlView.setText("Pool URL: " + mServer);
                                Toast.makeText(getContext(),"" + mServer ,Toast.LENGTH_SHORT).show();
                            }
                        });
                        AlertDialog dialog = alert.create();
                        dialog.show();
                        newServerFlag = true;
                        serverList.setSelection(0);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //NOTHING
            }
        });


    }

    void popupMsg(View parentView,String msg) {
        //RelativeLayout mRelativeLayout;
        final TextView msgView;
        final PopupWindow mPopupWindow;


        //mRelativeLayout = (RelativeLayout)getActivity().findViewById(R.id.popup_layout);


        LayoutInflater inflater = LayoutInflater.from(getContext());
        //LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.simple_popup_window,null);

        mPopupWindow = new PopupWindow(popupView, RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        ImageButton closeButton = (ImageButton) popupView.findViewById(R.id.ib_close);
        msgView = (TextView)popupView.findViewById(R.id.tv);
        msgView.setText(msg);

        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                mPopupWindow.dismiss();
            }
        });
        mPopupWindow.showAtLocation(parentView, Gravity.CENTER,0,0);
    }

    boolean verifySettings() {
        int networkStat = NetworkUtil.getConnectivityStatus(getActivity());
        //Handle Network
        boolean only_wifi =  prefs.getBoolean(getString(R.string.settings_wifi_key),Boolean.parseBoolean(getString(R.string.settings_wifi_default)));
        if (networkStat == NetworkUtil.TYPE_NOT_CONNECTED) {
            Toast.makeText(getActivity(),"No Internet Connection", Toast.LENGTH_LONG).show();
            popupMsg(mView,"No Internet Connection, please refer to Settings");
            return false;
        }
        if (only_wifi && networkStat != NetworkUtil.TYPE_WIFI) {
            Toast.makeText(getActivity(),"No Wi-Fi", Toast.LENGTH_LONG).show();
            popupMsg(mView,"No Wi-Fi Connection, please refer to Settings");
            return false;
        }

        //Handle Battery
        boolean only_charger =  prefs.getBoolean(getString(R.string.settings_charger_key),Boolean.parseBoolean(getString(R.string.settings_charger_default)));
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getActivity().registerReceiver(null, ifilter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        if (only_charger && isCharging == false) {
            Toast.makeText(getActivity(),"Charger Not Connected", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Charger Not Connected, please refer to Settings");
            return false;
        }

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float)scale;

        if (batteryPct<0.3) {
            Toast.makeText(getActivity(),"Low Battery Level", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Low Battery Level, please charge your device");
            return false;
        }

        //Handle parameters:
        if (mServer.startsWith(getString(R.string.pool_url_format)) == false) {
            Toast.makeText(getActivity(),"Illegal URL Address", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Illegal Server Address, must start with: " + getString(R.string.pool_url_format));
            return false;
        }
        if (mServer.matches(".*[:]\\d+") == false) {
            Toast.makeText(getActivity(),"Illegal URL Address", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Illegal Server Address, Pool Address format: " + getString(R.string.pool_url_format) + "example.org:port_number");
            return false;
        }

        if (mUser.equals("")) {
            Toast.makeText(getActivity(),"Illegal Username", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Illegal Username, please refer to Settings");
            return false;
        }

        if (mPassword.equals("")) {
            Toast.makeText(getActivity(),"Illegal Password", Toast.LENGTH_LONG).show();
            popupMsg(mView,"Illegal Password, please refer to Settings");
            return false;
        }

        return true;
    }

    //
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
    public class CustomSpinnerAdapter extends BaseAdapter implements SpinnerAdapter {

        private final Context activity;
        private ArrayList<String> asr;

        public CustomSpinnerAdapter(Context context,ArrayList<String> asr) {
            this.asr=asr;
            activity = context;
        }



        public int getCount()
        {
            return asr.size();
        }

        public Object getItem(int i)
        {
            return asr.get(i);
        }

        public long getItemId(int i)
        {
            return (long)i;
        }



        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView txt = new TextView(getActivity());
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(18);
            txt.setGravity(Gravity.CENTER_VERTICAL);
            txt.setText(asr.get(position));
            txt.setTextColor(Color.parseColor("#000000"));
            return  txt;
        }

        public View getView(int i, View view, ViewGroup viewgroup) {
            TextView txt = new TextView(getActivity());
            txt.setGravity(Gravity.CENTER);
            txt.setPadding(16, 16, 16, 16);
            txt.setTextSize(14);
            txt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down, 0);
            txt.setText(asr.get(i));
            txt.setTextColor(Color.parseColor("#000000"));
            return  txt;
        }

    }
}
