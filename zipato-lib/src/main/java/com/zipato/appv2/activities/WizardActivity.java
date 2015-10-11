/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.activities;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.Translated;
import com.zipato.appv2.R;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.model.wizard.WizardField;
import com.zipato.model.wizard.WizardStep;
import com.zipato.util.TypeFaceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by murielK on 4/2/2015.
 */
public class WizardActivity extends BaseActivity {

    private static final String TAG = WizardActivity.class.getSimpleName();
    private static final int DEFAULT_COUNTDOWN = 60;

    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.buttonStep)
    TextView butStep;
    @InjectView(id.textViewTitle)
    @SetTypeFace("helvetica_neue_light.otf")
    TextView title;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewSubTitle)
    TextView titleSubtitle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewContent)
    TextView content;
    @InjectView(id.listViewTypeFiled)
    ListView listViewTypeField;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewCounter)
    TextView textViewCounter;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("cancel")
    @InjectView(id.buttonCancel)
    Button buttonCancel;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.buttonOk)
    Button buttonOk;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("next")
    @InjectView(id.buttonNext)
    Button buttonNext;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Translated("repeat")
    @InjectView(id.buttonRep)
    Button buttonRepeat;
    @InjectView(id.progressBar)
    ProgressBar progressBar;

    @Inject
    TypeFaceUtils typeFaceUtils;

    private int currentCounter;
    private WizardStep wizardStep;
    private Handler handler;
    private FieldViewAdapter fieldViewAdapter;

    private Timer timer;
    private boolean isCounting;

    private static void setListViewHeight(ListView listView) {
        final ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        final int desiredWidth = MeasureSpec.makeMeasureSpec(listView.getWidth(), MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private static Map<String, String> genWizardFieldMap(Iterable<WizardField> wizardFieldList) {
        final Map<String, String> map = new HashMap<>();
        for (WizardField wizardField : wizardFieldList) {
            map.put(wizardField.getName(), wizardField.getValue());
        }
        return map;
    }

    @Override
    protected void onPreContentView(Bundle savedInstanceState) {
    }

    @Override
    protected int getContentViewID() {
        return R.layout.wizard_activity;
    }

    @Override
    protected void onPostContentView(Bundle savedInstanceState) {
        ButterKnife.inject(this);
        typeFaceUtils.applyTypefaceFor(this);
        handler = new Handler();
        fieldViewAdapter = new FieldViewAdapter(this, null);
        listViewTypeField.setAdapter(fieldViewAdapter);
        reset();
    }

    @Override
    protected boolean provideMenu() {
        return false;
    }

    private void updateViews() {
        Log.d(TAG, wizardStep.toString());
        buttonRepeat.setVisibility(wizardStep.isRepeat() ? View.VISIBLE : View.GONE);
        buttonRepeat.setText((wizardStep.getRepeatLabel() == null) ? "" : wizardStep.getRepeatLabel());
        buttonOk.setVisibility(wizardStep.isFinished() ? View.VISIBLE : View.GONE);
        buttonCancel.setVisibility(wizardStep.isCancel() ? View.VISIBLE : View.GONE);
        buttonCancel.setText((wizardStep.getCancelLabel() == null) ? "" : wizardStep.getCancelLabel());
        buttonNext.setVisibility(wizardStep.isNext() ? View.VISIBLE : View.GONE);
        buttonNext.setText((wizardStep.getNextLabel() == null) ? "" : wizardStep.getNextLabel());
        title.setText((wizardStep.getTitle() == null) ? "" : wizardStep.getTitle());
        //titleSubtitle.setText();
        content.setText((wizardStep.getBody() == null) ? "" : Html.fromHtml(wizardStep.getBody()));
        butStep.setVisibility(View.VISIBLE);
        if (wizardStep.getStep() == 0)
            butStep.setVisibility(View.INVISIBLE);
        butStep.setText(languageManager.translate("step") + ' ' + wizardStep.getStep());
        fieldViewAdapter.setList(wizardStep.getFields());
        if (wizardStep.isCountdown()) {
            textViewCounter.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        } else if (wizardStep.isPoll()) {
            progressBar.setVisibility(View.VISIBLE);
            textViewCounter.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            textViewCounter.setVisibility(View.GONE);

        }
        setListViewHeight(listViewTypeField);
    }

    private void onTransactionUpdate() {
        if (!isOk())
            return;
        Log.d(TAG, "setting Views");
        updateViews();
        if (wizardStep.isPoll()) {
            if (wizardStep.isCountdown() && !isCounting) {
                Log.d(TAG, "wizardStep is countdown == true");
                currentCounter = (wizardStep.getCountdownUntil() == null) ? DEFAULT_COUNTDOWN : (int) ((wizardStep.getCountdownUntil().getTime() - System.currentTimeMillis()) / 1000);
                currentCounter = (currentCounter < 0) ? DEFAULT_COUNTDOWN : currentCounter;
                startCounter();
            }

            Log.d(TAG, "wizardStep is poll == true");
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    fetchTransaction(TransParam.POLL);
                }
            }, (long) (wizardStep.getPollInterval() * 1000));

        } else if (isCounting) {
            Log.d(TAG, "stopping counter...");
            stopCounter();
        }
    }

    private void stopCounter() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        isCounting = false;
    }

    private void startCounter() {
        stopCounter();
        timer = new Timer();
        TimerTask counter = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, String.format("counter = %d", currentCounter));
                        if (currentCounter == 0) {
                            stopCounter();
                        }
                        textViewCounter.setText(String.valueOf(currentCounter));
                        currentCounter -= 1;
                    }
                });
            }
        };
        isCounting = true;
        timer.scheduleAtFixedRate(counter, 0, 1000);
    }

    @OnClick(id.buttonNext)
    public void onNextClick(View v) {
        if (wizardStep.getStepCount() == 0)
            createTransaction();
        else
            fetchTransaction(TransParam.NEXT);
    }

    @OnClick(id.buttonCancel)
    public void onCancelClick(View v) {
        fetchTransaction(TransParam.CANCEL);
    }

    @OnClick(id.buttonOk)
    public void onOkClick(View v) {
        finish();
    }

    @OnClick(id.buttonRep)
    public void onRepeatClick(View v) {
        fetchTransaction(TransParam.REPEAT);
    }

    private boolean isOk() {
        if ((wizardStep == null) || (wizardStep.getStepCount() == 0)) {
            Log.d(TAG, "wizardStep is not OK resetting Views");
            reset();//TODO reset transaction here or something else??
            return false;
        }
        return true;
    }

    private void reset() { //initial wizard screen
        wizardStep = new WizardStep();
        buttonOk.setVisibility(View.GONE);
        buttonRepeat.setVisibility(View.GONE);
        buttonCancel.setVisibility(View.GONE);
        buttonCancel.setText(languageManager.translate("cancel"));
        buttonNext.setVisibility(View.VISIBLE);
        buttonNext.setText(languageManager.translate("next"));
        title.setText("Welcome to the Wizard");
        //titleSubtitle.setText();
        content.setText(languageManager.translate("wizard_init_desc"));
        textViewCounter.setText("");
        titleSubtitle.setText("");
        butStep.setVisibility(View.INVISIBLE);
        textViewCounter.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        WizardField wizardField = new WizardField();
        wizardField.setLabel(languageManager.translate("wizard_id"));
        wizardField.setDescription(languageManager.translate("wizard_id_des"));
        fieldViewAdapter.setList(Collections.singletonList(wizardField));
        setListViewHeight(listViewTypeField);

    }

    private void showProcessingOnUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog(languageManager.translate("processing"), false);
            }
        });
    }

    private void fetchTransaction(final TransParam trs) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, String.format("Fetching transaction type: %s transactionID: %s", trs.toString().toLowerCase(), wizardStep.getTransaction()));
                    switch (trs) {
                        case NEXT:
                            showProcessingOnUI();
                            if ((fieldViewAdapter.getList() != null) && !fieldViewAdapter.getList().isEmpty()) {
                                final Map<String, String> map = genWizardFieldMap(fieldViewAdapter.getList());
                                Log.d(TAG, "============== Field found ===========\n");
                                for (Entry<String, String> entry : map.entrySet()) {
                                    Log.d(TAG, String.format("\n %s: %s", entry.getKey(), entry.getValue()));
                                }
                                Log.d(TAG, "=====================================\n");
                                wizardStep = restTemplate.postForObject("v2/wizard/tx/{transaction}/{trsType}", map, WizardStep.class, wizardStep.getTransaction(), trs.toString().toLowerCase());
                            } else
                                wizardStep = restTemplate.getForObject("v2/wizard/tx/{transaction}/{trsType}", WizardStep.class, wizardStep.getTransaction(), trs.toString().toLowerCase());
                            break;
                        case POLL:
                            wizardStep = restTemplate.getForObject("v2/wizard/tx/{transaction}/{trsType}", WizardStep.class, wizardStep.getTransaction(), trs.toString().toLowerCase());
                            break;
                        case REPEAT:
                            showProcessingOnUI();
                            wizardStep = restTemplate.getForObject("v2/wizard/tx/{transaction}/{trsType}", WizardStep.class, wizardStep.getTransaction(), trs.toString().toLowerCase());
                            break;
                        case CANCEL:
                            showProcessingOnUI();
                            if (wizardStep.getTransaction() != null)
                                restTemplate.getForObject("v2/wizard/tx/{transaction}/{trsType}", WizardStep.class, wizardStep.getTransaction(), trs.toString().toLowerCase());
                            wizardStep = null;
                            break;

                    }
                } catch (Exception e) {
                    Log.d(TAG, "something when wrong while fetching transaction", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
//                            if (trs == TransParam.CANCEL)
//                                finish();
//                            else
                            onTransactionUpdate();
                        }
                    });
                }
            }
        });
    }

    private void createTransaction() {
        showProcessingOnUI();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String value = fieldViewAdapter.getList().get(0).getValue();
                    wizardStep = restTemplate.getForObject("v2/wizard/create/{name}", WizardStep.class, value);
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                } finally {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dismissProgressDialog();
                            onTransactionUpdate();
                        }
                    });
                }
            }
        });

    }

    private void cancelTransaction() { // in case the user force close the transaction
        if ((wizardStep != null) && (wizardStep.getTransaction() != null)) {
            Log.d(TAG, String.format("Force cancelling wizard with transaction ID: %s", wizardStep.getTransaction()));
            try {
                if (isCounting) {
                    Log.d(TAG, String.format("Force stopping current counter with current value: %d", currentCounter));
                    stopCounter();
                }
//                executor.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            restTemplate.getForObject("v2/wizard/tx/{transaction}/cancel", WizardStep.class, wizardStep.getTransaction());
//                        } catch (Exception e) {
//                            //
//                        }
//                    }
//                });
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelTransaction();

    }

    private enum TransParam {
        NEXT, POLL, REPEAT, CANCEL
    }

    class FieldViewAdapter extends BaseAdapter {

        private final Context context;
        private List<WizardField> list;

        public FieldViewAdapter(Context context, List<WizardField> list) {
            this.context = context;
            this.list = list;
        }

        public List<WizardField> getList() {
            return list;
        }

        public void setList(List<WizardField> list) {
            this.list = list;
            notifyDataSetChanged();

        }

        @Override
        public int getCount() {
            return (list == null) ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return (list == null) ? null : list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return (long) ((list == null) ? 0 : 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(layout.row_field_type, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.textViewTile.setText((list.get(position).getLabel() == null) ? "" : (list.get(position).getLabel() + ": "));
            final EditText editText = viewHolder.input;
            editText.setHint((list.get(position).getDescription() == null) ? "" : list.get(position).getDescription());
            editText.setTag(position);
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    final int pos = (int) editText.getTag();// i know useless could easily use the local position...
                    list.get(pos).setValue(s.toString());
                }
            });
            return convertView;
        }

        class ViewHolder {

            @SetTypeFace("helvetica_neue_light.otf")
            @InjectView(id.textViewFieldName)
            TextView textViewTile;
            @SetTypeFace("helvetica_neue_light.otf")
            @InjectView(id.editTextFieldInput)
            EditText input;

            public ViewHolder(View v) {
                ButterKnife.inject(this, v);
                typeFaceUtils.applyTypefaceFor(this);

            }
        }
    }
}
