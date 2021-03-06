package net.nhiroki.bluelineconsole.applicationMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import net.nhiroki.bluelineconsole.BuildConfig;
import net.nhiroki.bluelineconsole.R;
import net.nhiroki.bluelineconsole.interfaces.CandidateEntry;

import static android.view.inputmethod.EditorInfo.IME_FLAG_FORCE_ASCII;

public class MainActivity extends BaseWindowActivity {
    private CandidateListAdapter _resultCandidateListAdapter;
    private CommandSearchAggregator _commandSearchAggregator;
    private ExecutorService _threadPool;

    public static final String PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII = "pref_mainedittext_flagforceascii";
    public static final String PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH = "pref_mainedittext_hint_locale_english";
    public static final int REQUEST_CODE_FOR_COMING_BACK = 1;

    private boolean _camebackFlag = false;
    private boolean _paused = false;

    public MainActivity() {
        super(R.layout.main_activity_body, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setHeaderFooterTexts(getString(R.string.app_name), String.format(getString(R.string.displayedFullVersionString), BuildConfig.VERSION_NAME));

        AppNotification.update(this);

        final ListView candidateListView = findViewById(R.id.candidateListView);
        _resultCandidateListAdapter = new CandidateListAdapter(this, new ArrayList<CandidateEntry>(), candidateListView);
        candidateListView.setAdapter(_resultCandidateListAdapter);

        candidateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                _resultCandidateListAdapter.invokeEvent(position, MainActivity.this);
            }
        });

        candidateListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && v.onKeyDown(keyCode, event)) {
                    return true;
                }

                //noinspection RedundantIfStatement
                if (event.getAction() == KeyEvent.ACTION_UP && v.onKeyUp(keyCode, event)) {
                    return true;
                }

                return false;
            }
        });

        final EditText mainInputText = findViewById(R.id.mainInputText);
        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                mainInputText.setImeHintLocales(new LocaleList(new Locale("en")));
            }
        }

        // flagForceAscii enabled at layout.
        // Disabling on layout and enabling here did not resolve my problem.
        // Also it seems to go around every languages even disabled here
        if (! PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            mainInputText.setImeOptions(mainInputText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }
        mainInputText.requestFocus();
        mainInputText.requestFocusFromTouch();

        mainInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (_resultCandidateListAdapter.isEmpty()) {
                    return false;
                }
                if (event == null || event.getAction() != KeyEvent.ACTION_UP) {
                    _resultCandidateListAdapter.invokeFirstChoiceEvent(MainActivity.this);
                    return true;
                }
                return false;
            }
        });

        mainInputText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                    candidateListView.requestFocus();
                    candidateListView.requestFocusFromTouch();
                    return MainActivity.this._resultCandidateListAdapter.selectChosenNowAsListView() && candidateListView.onKeyDown(keyCode, event);
                }
                return false;
            }
        });
    }

    @Override
    public void onDestroy() {
        this._commandSearchAggregator.close();
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        this._paused = false;

        final EditText mainInputText = findViewById(R.id.mainInputText);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_FLAG_FORCE_ASCII, false)) {
            mainInputText.setImeOptions(mainInputText.getImeOptions() | IME_FLAG_FORCE_ASCII);
        } else {
            mainInputText.setImeOptions(mainInputText.getImeOptions() & ~IME_FLAG_FORCE_ASCII);
        }

        if (Build.VERSION.SDK_INT >= 24) {
            if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(PREF_KEY_MAIN_EDITTEXT_HINT_LOCALE_ENGLISH, false)) {
                mainInputText.setImeHintLocales(new LocaleList(new Locale("en")));
            } else {
                mainInputText.setImeHintLocales(null);
            }
        }

        if (!this.getCurrentTheme().equals(this.readThemeFromConfig())) {
            Intent intent = this.getPackageManager().getLaunchIntentForPackage("net.nhiroki.bluelineconsole");
            this.finish();
            this.startActivity(intent);
        }
        _threadPool = Executors.newSingleThreadExecutor();

        final boolean showStartUpHelp = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(StartUpHelpActivity.PREF_KEY_SHOW_STARTUP_HELP, true);

        if (_commandSearchAggregator == null) {
            // first time after onCreate()
            _commandSearchAggregator = new CommandSearchAggregator(this);

            if (!_camebackFlag && showStartUpHelp) {
                MainActivity.this._camebackFlag = true;
                startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
            }
            mainInputText.addTextChangedListener(new MainInputTextListener(mainInputText.getText()));

        } else {
            if (_camebackFlag) {
                List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(mainInputText.getText().toString(), MainActivity.this);
                _commandSearchAggregator.refresh(this);

                _resultCandidateListAdapter.clear();
                _resultCandidateListAdapter.addAll(cands);
                _resultCandidateListAdapter.notifyDataSetChanged();

                _camebackFlag = false;

            } else {
                _commandSearchAggregator.refresh(this);

                if (showStartUpHelp) {
                    MainActivity.this._camebackFlag = true;
                    startActivityForResult(new Intent(MainActivity.this, StartUpHelpActivity.class), MainActivity.REQUEST_CODE_FOR_COMING_BACK);
                }
                _resultCandidateListAdapter.clear();
                _resultCandidateListAdapter.notifyDataSetChanged();

                mainInputText.setText("");
            }
        }

        MainActivity.this.enableBaseWindowAnimation();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FOR_COMING_BACK && resultCode == RESULT_OK) {
            _camebackFlag = true;
        }
    }

    @Override
    protected void onPause() {
        _threadPool.shutdownNow();
        this._paused = true;
        super.onPause();
    }

    @Override
    protected void onHeightChange() {
        super.onHeightChange();
        this.setWholeLayout();
    }

    @Override
    protected void enableWindowAnimationForElements() {
        super.enableWindowAnimationForElements();

        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainRootLinearLayout));
        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.enableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    @Override
    protected void disableWindowAnimationForElements() {
        super.disableWindowAnimationForElements();

        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainRootLinearLayout));
        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.mainInputTextWrapperLinearLayout));
        this.disableWindowAnimationForEachViewGroup((ViewGroup) findViewById(R.id.candidateViewWrapperLinearLayout));
    }

    private void setWholeLayout() {
        final EditText mainInputText = findViewById(R.id.mainInputText);
        final boolean textFilled = ! mainInputText.getText().toString().equals("");

        this.setWindowBoundarySize(textFilled ? ROOT_WINDOW_FULL_WIDTH_IN_MOBILE : ROOT_WINDOW_ALWAYS_HORZONTAL_MARGIN, 0);

        this.setWindowLocationGravity(textFilled ? Gravity.TOP : Gravity.CENTER_VERTICAL);

        final double pixelsPerSp = getResources().getDisplayMetrics().scaledDensity;

        // mainInputText: editTextSize * (1 (text) + 0.3 * 2 (padding)
        // If space is limited, split remaining height into 1(EditText):2(ListView and other margins)
        final double editTextSizeSp = Math.min(40.0, this.getWindowBodyAvailableHeight() / 4.8 / pixelsPerSp);
        mainInputText.setTextSize((int) editTextSizeSp);
        mainInputText.setPadding((int) (editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp), (int)(editTextSizeSp * 0.3 * pixelsPerSp));

        mainInputText.requestFocus();
        mainInputText.requestFocusFromTouch();
    }

    private void executeSearch(CharSequence s) {
        List<CandidateEntry> cands = _commandSearchAggregator.searchCandidateEntries(s.toString(), MainActivity.this);

        _resultCandidateListAdapter.clear();
        _resultCandidateListAdapter.addAll(cands);
        _resultCandidateListAdapter.notifyDataSetChanged();

        if (cands.isEmpty()) {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, 0, 0, 0);
        } else {
            findViewById(R.id.candidateViewWrapperLinearLayout).setPaddingRelative(0, (int)(6 * getResources().getDisplayMetrics().density + 0.5), 0, 0);
        }

        setWholeLayout();
    }

    private void onCommandInput(final CharSequence s) {
        if (_commandSearchAggregator.isPrepared()) { // avoid waste waitUntilPrepared if already prepared
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
            executeSearch(s);
        } else {
            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.VISIBLE);
            _threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    _commandSearchAggregator.waitUntilPrepared();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (MainActivity.this._paused) {
                                // If activity is paused, doing something here is at least waste, sometimes dangerous
                                return;
                            }
                            executeSearch(s);
                            findViewById(R.id.commandSearchWaitingNotification).setVisibility(View.GONE);
                        }
                    });
                }
            });
        }
    }

    private class MainInputTextListener implements TextWatcher {
        public MainInputTextListener(CharSequence s) {
            if(! s.toString().equals("")) {
                onCommandInput(s);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onCommandInput(s);
        }

        @Override
        public void afterTextChanged(Editable s) { }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
    }
}
