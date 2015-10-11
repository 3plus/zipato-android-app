/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.zipato.annotation.SetTypeFace;
import com.zipato.annotation.ViewType;
import com.zipato.appv2.R;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.id;
import com.zipato.appv2.R.layout;
import com.zipato.appv2.ui.fragments.adapters.BaseListAdapter;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.AbsLevel.Queuer;
import com.zipato.appv2.ui.fragments.controller.viewcontrollers.AbsLevel.Queuer.DataObject;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.helper.LongPressHelper;
import com.zipato.helper.LongPressHelper.Builder;
import com.zipato.helper.LongPressHelper.LongPressController;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.thermostat.EnumOperation;
import com.zipato.model.thermostat.Operation;
import com.zipato.model.thermostat.Thermostat;
import com.zipato.model.thermostat.ThermostatRepository;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.translation.LanguageManager;
import com.zipato.util.TagFactoryUtils;
import com.zipato.util.TypeFaceUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnItemSelected;
import butterknife.OnTouch;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 8/27/2015.
 */
@ViewType(R.layout.view_controller_thermostat)
public class VCThermostat extends AbsHeader implements ViewControllerLogic, OnClickListener, OnLongClickListener, OnTouchListener, LongPressController {

    private static final String TAG = TagFactoryUtils.getTag(VCThermostat.class);
    private static final int THERMOSTAT_VIEW_TYPE_SINGLE = 0;
    private static final int THERMOSTAT_VIEW_TYPE_DUAL = 1;
    private static final int MIN_THERMO_VALUE = 5;
    private static final int MAX_THERMO_VALUE = 40;
    private static final double DEFAULT_HEAT_MARGIN = 0.5;
    private static final double DEFAULT_COOL_MARGIN = 0.5;
    private static final double DEFAULT_STEP = 0.1;
    private static final long DELAY_TO_SEND_COMMAND = 500L;

    private final List<ThermostatMode> thermostatModes = new ArrayList<>();
    private final List<ThermostatOperation> thermostatOperations = new ArrayList<>();
    private final LongPressHelper longPressHelper;
    private final Queuer queuer;
    @InjectView(id.spinnerMode)
    protected Spinner spinnerMode;
    @InjectView(id.spinnerOperation)
    protected Spinner spinnerOperation;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewCurrentValue)
    TextView textCurrentValue;
    @InjectView(id.tcSingle)
    LinearLayout layoutSingle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textValueSingle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textStatusSingle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textPlusSingle;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textMinusSingle;
    @InjectView(id.tcDual)
    LinearLayout layoutDual;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textValueHeating;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textStatusHeating;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textPlusHeating;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textMinusHeating;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textValueCooling;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textStatusCooling;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textPlusCooling;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    TextView textMinusCooling;
    @Inject
    ThermostatRepository thermostatRepository;
    private OperationListAdapter operationAdapter;
    private ModeListAdapter modeAdapter;

    private int controllerViewType;
    private int logicQueueID;
    private int currentInteractingControlID;
    private double marginHeat = DEFAULT_HEAT_MARGIN;
    private double marginCool = DEFAULT_COOL_MARGIN;
    private double heating = MIN_THERMO_VALUE;
    private double cooling = MAX_THERMO_VALUE;

    public VCThermostat(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        queuer = new Queuer(this);
        longPressHelper = new Builder(this).setMinInterval(80).setStepper(3).setDecInterval(100).setStartInterval(300).build();
        init();
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    protected static Operation getOperation(final Thermostat thermostat, final EnumOperation enumOperation) {
        if (thermostat == null)
            return null;
        if ((thermostat.getOperations() == null) || (thermostat.getOperations().length == 0))
            return null;
        final int index;
        try {
            index = resolveIndex(thermostat.getOperationIntMap(), enumOperation);
        } catch (NullPointerException e) {
            return null;
        }
        return thermostat.getOperations()[index];
    }

    private static <E extends Enum> int resolveIndex(final HashMap<E, Integer> map, final E enm) {
        if (map.get(enm) != null)
            return map.get(enm);
        throw new NullPointerException("No index found");
    }

    private static void setSelectionWithoutCallBack(final Spinner spinner, final int selection) {
        final OnItemSelectedListener listener = spinner.getOnItemSelectedListener();
        spinner.setOnItemSelectedListener(null);
        spinner.setSelection(selection, false);
        spinner.setOnItemSelectedListener(listener);
    }

    @Override
    protected int getMainIndexAttrToDisplay(TypeReportItem item) {
        return 0;
    }

    private void init() {
        //set the but for the single layout
        textStatusSingle = (TextView) layoutSingle.findViewById(id.textViewStatus);
        textValueSingle = (TextView) layoutSingle.findViewById(id.textViewValue);
        textMinusSingle = (TextView) layoutSingle.findViewById(id.buttonMinus);
        textPlusSingle = (TextView) layoutSingle.findViewById(id.buttonPlus);

        //set the but for the dual layout  - heating -
        final LinearLayout layoutHeating = (LinearLayout) layoutDual.findViewById(id.tcDualHeating);
        textStatusHeating = (TextView) layoutHeating.findViewById(id.textViewStatus);
        textValueHeating = (TextView) layoutHeating.findViewById(id.textViewValue);
        textMinusHeating = (TextView) layoutHeating.findViewById(id.buttonMinus);
        textPlusHeating = (TextView) layoutHeating.findViewById(id.buttonPlus);

        //set the but for the dual layout  - cooling -
        final LinearLayout layoutCooling = (LinearLayout) layoutDual.findViewById(id.tcDualCooling);
        textStatusCooling = (TextView) layoutCooling.findViewById(id.textViewStatus);
        textValueCooling = (TextView) layoutCooling.findViewById(id.textViewValue);
        textMinusCooling = (TextView) layoutCooling.findViewById(id.buttonMinus);
        textPlusCooling = (TextView) layoutCooling.findViewById(id.buttonPlus);

        textStatusHeating.setText(capitalizer(languageManager.translate("heating_at")));
        textStatusCooling.setText(capitalizer(languageManager.translate("cooling_at")));

        textMinusSingle.setOnClickListener(this);
        textPlusSingle.setOnClickListener(this);
        textPlusCooling.setOnClickListener(this);
        textMinusCooling.setOnClickListener(this);
        textPlusHeating.setOnClickListener(this);
        textMinusHeating.setOnClickListener(this);

        textMinusSingle.setOnLongClickListener(this);
        textPlusSingle.setOnLongClickListener(this);
        textPlusCooling.setOnLongClickListener(this);
        textMinusCooling.setOnLongClickListener(this);
        textPlusHeating.setOnLongClickListener(this);
        textMinusHeating.setOnLongClickListener(this);

        textMinusSingle.setOnTouchListener(this);
        textPlusSingle.setOnTouchListener(this);
        textPlusCooling.setOnTouchListener(this);
        textMinusCooling.setOnTouchListener(this);
        textPlusHeating.setOnTouchListener(this);
        textMinusHeating.setOnTouchListener(this);

        typeFaceUtils.applyTypefaceFor(this);

        thermostatModes.add(ThermostatMode.HOLD_PERIOD);
        thermostatModes.add(ThermostatMode.PROGRAM);
        thermostatModes.add(ThermostatMode.HOLD_PERMANENT);
        thermostatModes.add(ThermostatMode.HOLD_UNTIL);

        modeAdapter = new ModeListAdapter(getContext(), languageManager, typeFaceUtils);
        spinnerMode.setAdapter(modeAdapter);

        thermostatOperations.add(ThermostatOperation.OFF);

        operationAdapter = new OperationListAdapter(getContext(), languageManager, typeFaceUtils);
        spinnerOperation.setAdapter(operationAdapter);
    }

    private void handleSpinnerModeSelect(int position) {
        final ThermostatMode mode = thermostatModes.get(position);
        switch (mode) {
            case PROGRAM:
                sendMode(BaseTypesFragment.MODE, ThermostatMode.PROGRAM.name());
                break;
            case HOLD_PERIOD:
                sendMode(BaseTypesFragment.MODE, ThermostatMode.HOLD_PERIOD.name());
                break;
            case HOLD_PERMANENT:
                sendMode(BaseTypesFragment.MODE, ThermostatMode.HOLD_PERMANENT.name());
                break;
            case HOLD_UNTIL:
                sendTime();
                break;
        }
    }

    private void handleSpinnerOpClick(int position) {
        final Operation coolingOperation = getOperation(getThermostat(), EnumOperation.COOLING);
        final Operation heatingOperation = getOperation(getThermostat(), EnumOperation.HEATING);
        final ThermostatOperation thermostatOperation = thermostatOperations.get(position);
        boolean heatOpDisable = true;
        boolean coolOpDisable = true;
        switch (thermostatOperation) {
            case OFF:
                try {
                    sendAttributeValue(heatingOperation.getAttributes()[heatingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "true");
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                try {
                    sendAttributeValue(coolingOperation.getAttributes()[coolingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "true");
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                break;
            case AUTO:
                heatOpDisable = false;
                coolOpDisable = false;
                try {
                    sendAttributeValue(heatingOperation.getAttributes()[heatingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "false");
                    sendAttributeValue(coolingOperation.getAttributes()[coolingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "false");
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                break;
            case COOLING:
                heatOpDisable = true;
                coolOpDisable = false;
                try {
                    sendAttributeValue(coolingOperation.getAttributes()[coolingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "false");
                    sendAttributeValue(heatingOperation.getAttributes()[heatingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "true");
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                break;
            case HEATING:
                heatOpDisable = false;
                coolOpDisable = true;
                try {
                    sendAttributeValue(heatingOperation.getAttributes()[heatingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "false");
                    sendAttributeValue(coolingOperation.getAttributes()[coolingOperation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)].getUuid(), "true");
                } catch (Exception e) {
                    Log.d(TAG, "", e);
                }
                break;
        }

        updateControllerHeatCoolOp(heatOpDisable, coolOpDisable, true);
    }

    @Override
    public void longPressUpdate(final double current, final int viewID) {
        final Handler handler = getHandler();
        if (handler == null)
            return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateLocalTargetValues(current);
                updateTargetViewValues();
                sendTargets(DELAY_TO_SEND_COMMAND);
            }
        });
    }

    private void sendTargets() {
        sendTargets(0L);
    }

    private void sendTargets(long delay) {
        final Operation coolingOperation = getOperation(getThermostat(), EnumOperation.COOLING);
        final Operation heatingOperation = getOperation(getThermostat(), EnumOperation.HEATING);
        DataObject dataCooling = null;
        try {
            dataCooling = new DataObject(heatingOperation.getAttributes()[heatingOperation.getAttributeIntMap().get(BaseTypesFragment.TARGET)].getUuid(), String.valueOf(heating));
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

        DataObject dataHeating = null;
        try {
            dataHeating = new DataObject(coolingOperation.getAttributes()[coolingOperation.getAttributeIntMap().get(BaseTypesFragment.TARGET)].getUuid(), String.valueOf(cooling));
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }
        queuer.sendCommand(delay, dataCooling, dataHeating);

    }

    private void updateLocalTargetValues(double value) {
        final double marginLefRight = marginCool + marginHeat;

        switch (currentInteractingControlID) {
            case id.tcDualHeating:
                heating = value;
                if ((heating + marginLefRight) > (cooling))
                    cooling = heating + marginLefRight;
                break;
            case id.tcDualCooling:
                cooling = value;
                if ((cooling - marginLefRight) < (heating))
                    heating = cooling - marginLefRight;
                break;
            case id.tcSingle:
                if (thermostatOperations.contains(ThermostatOperation.HEATING))
                    heating = value;
                else cooling = value;
                break;
        }

        if (heating < (double) MIN_THERMO_VALUE)
            heating = (double) MIN_THERMO_VALUE;
        if (heating > ((double) MAX_THERMO_VALUE - (marginLefRight)))
            heating = (double) MAX_THERMO_VALUE - (marginLefRight);
        if (cooling > (double) MAX_THERMO_VALUE)
            cooling = (double) MAX_THERMO_VALUE;
        if (cooling < (MIN_THERMO_VALUE + (marginLefRight)))
            cooling = (double) MIN_THERMO_VALUE + (marginLefRight);
        heating = round(heating, 1);
        cooling = round(cooling, 1);


    }

    private void onClickPlusMinisControl(int id, double value) {
        switch (id) {
            case R.id.buttonMinus:
                value -= DEFAULT_STEP;
                break;
            case R.id.buttonPlus:
                value += DEFAULT_STEP;
                break;
        }

        updateLocalTargetValues(value);
        updateTargetViewValues();
        sendTargets(DELAY_TO_SEND_COMMAND);
    }

    private void onLongClickPlusMinisControl(int id, double value) {
        switch (id) {
            case R.id.buttonMinus:
                longPressHelper.start(value, true, id);
                break;
            case R.id.buttonPlus:
                longPressHelper.start(value, false, id);
                break;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int id = v.getId();
        switch (id) {
            case R.id.buttonPlus:
            case R.id.buttonMinus:
                if ((event.getAction() == MotionEvent.ACTION_CANCEL) || ((event.getAction() == MotionEvent.ACTION_UP))) {
                    longPressHelper.cancel();
                    enableRecyclerScrolling();
                    resetAdapterUpdate(ViewController.DEFAULT_RESET_DELAY);
                }
                break;
        }
        return false;
    }

    @Override
    public boolean onLongClick(View v) {
        disableRecyclerScrolling();
        disableAdapterUpdate();

        currentInteractingControlID = ((View) v.getParent().getParent()).getId();

        final int childID = v.getId();

        switch (currentInteractingControlID) {
            case id.tcDualHeating:
                onLongClickPlusMinisControl(childID, heating);
                break;
            case id.tcDualCooling:
                onLongClickPlusMinisControl(childID, cooling);
                break;
            case id.tcSingle:
                if (thermostatOperations.contains(ThermostatOperation.COOLING))
                    onLongClickPlusMinisControl(childID, cooling);
                else onLongClickPlusMinisControl(childID, heating);
                break;
        }

        return true;
    }

    @Override
    public void onClick(View v) {
        defaultBlockResetUpdate();

        currentInteractingControlID = ((View) v.getParent().getParent()).getId();

        final int childID = v.getId();

        switch (currentInteractingControlID) {
            case id.tcDualHeating:
                onClickPlusMinisControl(childID, heating);
                break;
            case id.tcDualCooling:
                onClickPlusMinisControl(childID, cooling);
                break;
            case id.tcSingle:
                if (thermostatOperations.contains(ThermostatOperation.HEATING))
                    onClickPlusMinisControl(childID, heating);
                else onClickPlusMinisControl(childID, cooling);
                break;
        }
    }


    @OnItemSelected(id.spinnerMode)
    public void onSpinnerModeItemSelected(int position) {
        handleSpinnerModeSelect(position);
    }

    @OnItemSelected(id.spinnerOperation)
    public void onSpinnerOperationItemSelected(int position) {
        handleSpinnerOpClick(position);
    }

    @OnTouch(id.spinnerMode)
    public boolean onSpinnerModeTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP)
            defaultBlockResetUpdate();
        return false;
    }

    @OnTouch(id.spinnerOperation)
    public boolean onSpinneOperationTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP)
            defaultBlockResetUpdate();
        return false;
    }

    @Override
    public boolean hasLogic() {
        return true;
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        final Thermostat thermostat = getThermostat((TypeReportItem) object);
        if (thermostat == null) {
            textCurrentValue.setText(capitalizer(languageManager.translate("loading_box")));
            return;
        }

        updateController(thermostat);
    }

    @Override
    public void setLogicQueueID(int logicQueueID) {
        this.logicQueueID = logicQueueID;
    }

    @Override
    public void run() {
        final ThreadLocal<Integer> local = new ThreadLocal<>();
        final GenericAdapter genericAdapter = getAdapter();
        local.set(logicQueueID);
        boolean success = false;
        try {
            if (thermostatRepository.isEmpty())
                thermostatRepository.fetchAll();
            success = true;
        } catch (Exception e) {
            Log.e(TAG, "", e);
        } finally {
            final int viewType = layout.view_controller_thermostat;
            if ((genericAdapter != null) && success) {
                genericAdapter.logicExecuted(viewType, true, local.get());
            } else if (genericAdapter != null)
                genericAdapter.logicFailExecution(viewType, local.get());

        }
    }

    private void sendMode(final int attrID, final String value) {
        final Operation masterOperation = getOperation(getThermostat(), EnumOperation.MASTER);
        if (masterOperation == null)
            return;
        Attribute attr = masterOperation.getAttributes()[masterOperation.getAttributeIntMap().get(attrID)];
        if (attr.getAttributeId() == attrID) {
            sendAttributeValue(attr.getUuid(), value);
        }
    }

    protected void sendTime() {
        sendMode(BaseTypesFragment.MODE, ThermostatMode.HOLD_UNTIL.name());
        final Calendar c = Calendar.getInstance();
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);

        final TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), new OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                sendMode(BaseTypesFragment.MODE, ThermostatMode.HOLD_UNTIL.name());
                sendMode(BaseTypesFragment.HOLD_UNTIL, String.valueOf(c.getTimeInMillis()));

            }
        }, mHour, mMinute, true);

        timePickerDialog.show();
    }

    private void updateTargetViewValues() {
        //TODO verify value base on margins
        switch (controllerViewType) {
            case THERMOSTAT_VIEW_TYPE_SINGLE:
                if (thermostatOperations.contains(ThermostatOperation.HEATING))
                    textValueSingle.setText(heating + BaseTypesFragment.DEGREE);
                else if (thermostatOperations.contains(ThermostatOperation.COOLING))
                    textValueSingle.setText(cooling + BaseTypesFragment.DEGREE);
                else textValueSingle.setText("- -");
                break;
            case THERMOSTAT_VIEW_TYPE_DUAL:
                textValueHeating.setText(heating + BaseTypesFragment.DEGREE);
                textValueCooling.setText(cooling + BaseTypesFragment.DEGREE);
                break;
        }
    }

    private double getActualValueFor(Operation operation) {
        final Attribute actualAttr = operation.getAttributes()[operation.getAttributeIntMap().get(BaseTypesFragment.ACTUAL)];
        return Double.valueOf(getValueForAttr(actualAttr.getUuid()));

    }

    private double getTargetValueFor(Operation operation) {
        final Attribute attrTarget = operation.getAttributes()[operation.getAttributeIntMap().get(BaseTypesFragment.TARGET)];
        return Double.valueOf(getValueForAttr(attrTarget.getUuid()));

    }

    private boolean isOperationDisable(Operation operation) {
        final Attribute attrDisable = operation.getAttributes()[operation.getAttributeIntMap().get(BaseTypesFragment.DISABLE)];
        return "true".equalsIgnoreCase(getValueForAttr(attrDisable.getUuid()));
    }

    private String getMode(int mode) {
        final Operation masterOperation = getOperation(getThermostat(), EnumOperation.MASTER);
        if (masterOperation == null)
            return "";

        final Attribute attribute = masterOperation.getAttributes()[masterOperation.getAttributeIntMap().get(mode)];
        return getValueForAttr(attribute.getUuid());
    }


    private void applyControllerMode() {
        final String mode = getMode(BaseTypesFragment.MODE);
        final int index = modeAdapter.getSelectedItemPosition(mode);

        if (index >= 0)
            setSelectionWithoutCallBack(spinnerMode, index);

        modeAdapter.notifyDataSetChanged();
    }

    private void applyControllerOperations(Operation opHeating, Operation opCooling) {
        controllerViewType = THERMOSTAT_VIEW_TYPE_SINGLE;

        if (thermostatOperations.size() > 1) { //nicely reset operation list

            final int size = thermostatOperations.size();
            for (int i = size - 1; i > 0; i--) {
                thermostatOperations.remove(i);
            }

        }

        boolean coolOpDisable = true;
        if ((opCooling != null) && (opCooling.getOutputs() != null) && (opCooling.getOutputs().length > 0)) {
            thermostatOperations.add(ThermostatOperation.COOLING);
            try {
                coolOpDisable = isOperationDisable(opCooling);
            } catch (Exception e) {
                //Empty
            }
        }
        boolean heatOpDisable = true;
        if ((opHeating != null) && (opHeating.getOutputs() != null) && (opHeating.getOutputs().length > 0)) {
            thermostatOperations.add(ThermostatOperation.HEATING);
            try {
                heatOpDisable = isOperationDisable(opHeating);
            } catch (Exception e) {
                //Empty
            }

            if (thermostatOperations.contains(ThermostatOperation.COOLING)) {
                thermostatOperations.add(ThermostatOperation.AUTO);
                controllerViewType = THERMOSTAT_VIEW_TYPE_DUAL;
            }
        }

        swapViews();

        //set Operation base on heatMode and coolMode : true mean off
        updateControllerHeatCoolOp(heatOpDisable, coolOpDisable, false);

        operationAdapter.notifyDataSetChanged();
    }

    private void swapViews() {
        switch (controllerViewType) {
            case THERMOSTAT_VIEW_TYPE_SINGLE:
                if (View.GONE == layoutSingle.getVisibility()) {

                    layoutDual.setVisibility(View.GONE);
                    layoutSingle.setVisibility(View.VISIBLE);
                }

                if (thermostatOperations.contains(ThermostatOperation.COOLING))
                    textStatusSingle.setText(capitalizer(languageManager.translate("cooling_at")));
                else if (thermostatOperations.contains(ThermostatOperation.HEATING))
                    textStatusSingle.setText(capitalizer(languageManager.translate("heating_at")));
                else {
                    textStatusSingle.setText(capitalizer(languageManager.translate("- -")));
                    textValueSingle.setText("- -");
                }
                break;
            case THERMOSTAT_VIEW_TYPE_DUAL:
                if (View.VISIBLE == layoutDual.getVisibility())
                    return;

                layoutDual.setVisibility(View.VISIBLE);
                layoutSingle.setVisibility(View.GONE);
                break;
        }
    }

    protected void updateController(Thermostat thermostat) {
        Operation coolingOperation = null;

        try {
            coolingOperation = getOperation(thermostat, EnumOperation.COOLING);
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

        Operation heatingOperation = null;
        try {
            heatingOperation = getOperation(thermostat, EnumOperation.HEATING);
        } catch (Exception e) {
            Log.d(TAG, "", e);
            if (coolingOperation == null)
                return;
        }

        boolean tempNotSet = true;
        try {
            if (heatingOperation != null) {
                updateCurrentTemperature(getActualValueFor(heatingOperation));
                tempNotSet = false;
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
        } finally {
            try {
                if (tempNotSet)
                    updateCurrentTemperature(getActualValueFor(coolingOperation));
            } catch (Exception e) {
                Log.d(TAG, "", e);
            }

        }

        applyControllerMode();
        applyControllerOperations(heatingOperation, coolingOperation);

        //set progress heatValue, coolValue
        try {
            if (heatingOperation != null) {
                heating = getTargetValueFor(heatingOperation);
                marginHeat = heatingOperation.getConfig().getHysteresis();
            }

        } catch (Exception e) {
            Log.d(TAG, "", e);

        }

        try {
            if (coolingOperation != null) {
                cooling = getTargetValueFor(coolingOperation);
                marginCool = coolingOperation.getConfig().getHysteresis();
            }
        } catch (Exception e) {

            Log.d(TAG, "", e);
        }

        updateTargetViewValues();

    }

    private void updateControllerHeatCoolOp(boolean heatOpDisable, boolean coolOpDisable, boolean fromUser) { //  this is an expensive call :(
        final Context context = getContext();
        switch (controllerViewType) {
            case THERMOSTAT_VIEW_TYPE_SINGLE:
                textValueSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                textPlusSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                textMinusSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                textStatusSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                textMinusSingle.setEnabled(false);
                textPlusSingle.setEnabled(false);

                if (!coolOpDisable || !heatOpDisable) {
                    if (!coolOpDisable)
                        textValueSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_cooling));
                    else
                        textValueSingle.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_heating));
                    textPlusSingle.setTextColor(context.getResources().getColor(color.color_white));
                    textMinusSingle.setTextColor(context.getResources().getColor(color.color_white));
                    textStatusSingle.setTextColor(context.getResources().getColor(color.color_white));
                    textMinusSingle.setEnabled(true);
                    textPlusSingle.setEnabled(true);
                }

                break;
            case THERMOSTAT_VIEW_TYPE_DUAL:
                if (!heatOpDisable) {
                    textValueHeating.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_heating));
                    textMinusHeating.setTextColor(context.getResources().getColor(color.color_white));
                    textPlusHeating.setTextColor(context.getResources().getColor(color.color_white));
                    textStatusHeating.setTextColor(context.getResources().getColor(color.color_white));
                    textMinusHeating.setEnabled(true);
                    textPlusHeating.setEnabled(true);
                } else {
                    textValueHeating.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textMinusHeating.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textPlusHeating.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textStatusHeating.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textMinusHeating.setEnabled(false);
                    textPlusHeating.setEnabled(false);
                }

                if (!coolOpDisable) {
                    textValueCooling.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_cooling));
                    textMinusCooling.setTextColor(context.getResources().getColor(color.color_white));
                    textPlusCooling.setTextColor(context.getResources().getColor(color.color_white));
                    textStatusCooling.setTextColor(context.getResources().getColor(color.color_white));
                    textMinusCooling.setEnabled(true);
                    textPlusCooling.setEnabled(true);
                } else {
                    textValueCooling.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textMinusCooling.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textPlusCooling.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textStatusCooling.setTextColor(context.getResources().getColor(color.color_view_controller_thermo_disable));
                    textMinusCooling.setEnabled(false);
                    textPlusCooling.setEnabled(false);
                }

                break;
        }

        if (fromUser)
            return;

        if (heatOpDisable && coolOpDisable) {
            setSelectionWithoutCallBack(spinnerOperation, 0);
        } else if (heatOpDisable) {
            final int index = operationAdapter.getSelectedItemPosition(ThermostatOperation.COOLING.name());
            if (index >= 0)
                setSelectionWithoutCallBack(spinnerOperation, index);
        } else if (coolOpDisable) {
            final int index = operationAdapter.getSelectedItemPosition(ThermostatOperation.HEATING.name());
            if (index >= 0)
                setSelectionWithoutCallBack(spinnerOperation, index);
        } else {
            final int index = operationAdapter.getSelectedItemPosition(ThermostatOperation.AUTO.name());
            if (index >= 0)
                setSelectionWithoutCallBack(spinnerOperation, index);
        }
    }

    private void updateCurrentTemperature(double temperature) {
        final String currentTemp = languageManager.translate("t_currently_value").replace("{temp_value}", String.valueOf(temperature + BaseTypesFragment.DEGREE));
        textCurrentValue.setText(currentTemp);
    }

    private Thermostat getThermostat(TypeReportItem typeReportItem) {
        return thermostatRepository.get(typeReportItem.getUuid());
    }

    private Thermostat getThermostat() {
        final TypeReportItem typeReportItem = getTypeReportItem();
        if (typeReportItem == null) {
            Log.e(TAG, "typeReportItem == null on getThermostat method");
            return null;
        }

        return getThermostat(typeReportItem);
    }

    public enum ThermostatOperation {
        HEATING, COOLING, AUTO, OFF
    }


    protected enum ThermostatMode {
        PROGRAM, HOLD_PERIOD, HOLD_PERMANENT, HOLD_UNTIL
    }

    abstract static class BaseThermosSpinnerListAdapter extends BaseListAdapter {
        final Context context;
        final LanguageManager languageManager;
        final TypeFaceUtils typeFaceUtils;

        public BaseThermosSpinnerListAdapter(Context context, LanguageManager languageManager, TypeFaceUtils typeFaceUtils) {
            this.context = context;
            this.languageManager = languageManager;
            this.typeFaceUtils = typeFaceUtils;
        }

        abstract <E extends Enum> List<E> provideEnumList();

        public int getSelectedItemPosition(String input) {
            for (int i = 0; i < provideEnumList().size(); i++) {

                if (provideEnumList().get(i).name().equals(input)) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.simple_spinner_dropdown_item, parent, false);
                textView = (TextView) convertView.findViewById(R.id.text1);
                textView.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
                convertView.setTag(textView);

            } else {
                textView = (TextView) convertView.getTag();

            }
            textView.setText(capitalizer(languageManager.translate(provideEnumList().get(position).name().toLowerCase())));
            return convertView;
        }

        @Override
        public int getCount() {
            return provideEnumList().size();
        }

        @Override
        public Object getItem(int position) {
            return provideEnumList().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(layout.simple_spinner_item, parent, false);
                textView = (TextView) convertView.findViewById(id.text1);
                textView.setTypeface(typeFaceUtils.getTypeFace("helveticaneue_ultra_light.otf"));
                convertView.setTag(textView);
            } else {
                textView = (TextView) convertView.getTag();
            }
            textView.setText(capitalizer(languageManager.translate(provideEnumList().get(position).name().toLowerCase())));
            return convertView;
        }
    }

    class OperationListAdapter extends BaseThermosSpinnerListAdapter {

        public OperationListAdapter(Context context, LanguageManager languageManager, TypeFaceUtils typeFaceUtils) {
            super(context, languageManager, typeFaceUtils);
        }

        @Override
        <E extends Enum> List<E> provideEnumList() {
            return (List<E>) thermostatOperations;
        }
    }

    class ModeListAdapter extends BaseThermosSpinnerListAdapter {
        private static final long MIN_TO_MILLIS = 60000L;
        private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        public ModeListAdapter(Context context, LanguageManager languageManager, TypeFaceUtils typeFaceUtils) {
            super(context, languageManager, typeFaceUtils);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            convertView = super.getView(position, convertView, parent);

            if (provideEnumList().get(position) == ThermostatMode.HOLD_UNTIL) {

                long duration = 0;

                try {
                    String input = getMode(BaseTypesFragment.HOLD_UNTIL);
                    DateFormat df = (DateFormat) dateFormat.clone();
                    Date date = df.parse(input);
                    duration = date.getTime();
                } catch (Exception e) {
                    Log.d(TagFactoryUtils.getTag(this), "", e);
                }

                duration -= System.currentTimeMillis();
                duration = (duration < 0) ? 0 : duration;
                duration /= MIN_TO_MILLIS;
                String name = languageManager.translate(provideEnumList().get(position).toString().toLowerCase());

                TextView textView = (TextView) convertView.getTag();
                textView.setText(name + ' ' + duration + ' ' + languageManager.translate("min"));

            }

            return convertView;
        }

        @Override
        <E extends Enum> List<E> provideEnumList() {
            return (List<E>) thermostatModes;
        }
    }

}
