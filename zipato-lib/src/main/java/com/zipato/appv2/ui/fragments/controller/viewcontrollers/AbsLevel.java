/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.B;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.controller.ViewController;
import com.zipato.helper.LongPressHelper;
import com.zipato.helper.LongPressHelper.Builder;
import com.zipato.helper.LongPressHelper.LongPressController;
import com.zipato.model.attribute.Attribute;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;

import java.lang.ref.WeakReference;
import java.util.UUID;

import butterfork.Bind;
import butterfork.OnClick;
import butterfork.OnLongClick;
import butterfork.OnTouch;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 7/29/2015.
 */
public abstract class AbsLevel extends AbsBaseSimpleStatus implements LongPressController {

    public static final long DELAY_TO_SEND_COMMAND = 800L;
    private static final String TAG = TagFactoryUtils.getTag(AbsLevel.class);
    protected final Queuer queuer;
    protected final LongPressHelper longPressHelper;
    protected int current;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonMinus)
    TextView buttonMinus;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @Bind(B.id.buttonPlus)
    TextView buttonPlus;

    protected AbsLevel(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        queuer = new Queuer(this);
        longPressHelper = new Builder(this).build();
    }

    @Override
    public void dispatchOnBind(Object object) {
        super.dispatchOnBind(object);
        current = getCurrentValue();
    }

    protected abstract int getTargetAttrID();

    @Override
    public void longPressUpdate(final double current, int viewID) {
        if (textViewValue == null)
            return;
        textViewValue.post(new Runnable() {
            @Override
            public void run() {
                AbsLevel.this.current = (int) current;
                update(String.valueOf((int) current), DELAY_TO_SEND_COMMAND);
            }
        });
    }

    protected void update(String value, long delay) {
        final TypeReportItem item = getTypeReportItem();
        if (item == null) { // i know useless check
            Log.e(TAG, String.format("null item on @ %s returning", "update method"));
            return;
        }
        final Attribute attribute = getTypeAttributeFor(getTargetAttrID(), item);
        if (attribute == null) {
            Log.e(TAG, String.format("null attribute on @ %s returning", "update method"));
            return;
        }
        queuer.sendCommand(attribute.getUuid(), value, delay);
        value = capitalizer(attrValueUnitResolver(attribute.getUuid(), value));
        if (isCustomUnit()) {
            final String tempCustomUnit = getCustomUnit(attribute);
            if ((tempCustomUnit != null) && !value.contains(tempCustomUnit))
                value += tempCustomUnit;
        }
        textViewAtrName.setText(capitalizer(languageManager.translate(attribute.getName())));
        textViewValue.setText(value);
    }

    private void handleOnTouch(View v, MotionEvent event) {
        if ((v.getId() != id.buttonMinus) && (v.getId() != id.buttonPlus))
            return;

        if ((event.getAction() == MotionEvent.ACTION_CANCEL) || (event.getAction() == MotionEvent.ACTION_UP)) {
            longPressHelper.cancel();
            enableRecyclerScrolling();
            resetAdapterUpdate(ViewController.DEFAULT_RESET_DELAY);
        }
    }

    protected int getCurrentValue() {
        final TypeReportItem item = getTypeReportItem();
        if (item == null) { // i know useless check
            Log.e(TAG, String.format("null item on @ %s returning", "update method"));
            return 0;
        }

        final Attribute attribute = getTypeAttributeFor(getTargetAttrID(), item);
        if (attribute == null) {
            Log.e(TAG, String.format("null attribute on @ %s returning", "update method"));
            return 0;
        }

        double cv = 0;
        try {
            //cv = Double.valueOf(textViewValue.getText().toString().replace("%", ""));
            cv = Double.valueOf(getValueForAttr(attribute.getUuid()));
        } catch (Exception e) {
            //
        }
        return (int) cv;
    }

    @OnTouch(B.id.buttonPlus)
    public boolean onTouchPlus(View v, MotionEvent event) {
        handleOnTouch(v, event);
        return false;
    }

    @OnTouch(B.id.buttonMinus)
    public boolean onTouchMinus(View v, MotionEvent event) {
        handleOnTouch(v, event);
        return false;
    }

    @OnLongClick(B.id.buttonPlus)
    public boolean onPlusLongClick(View v) {
        disableRecyclerScrolling();
        disableAdapterUpdate();
        longPressHelper.start(current, false, textViewValue.getId());
        return true;
    }

    @OnLongClick(B.id.buttonMinus)
    public boolean onMinusLongClick(View v) {
        disableRecyclerScrolling();
        disableAdapterUpdate();
        longPressHelper.start(current, true, textViewValue.getId());
        return true;
    }

    @OnClick(B.id.buttonMinus)
    public void onMinusClick(View v) {
        defaultBlockResetUpdate();
        current -= 1;
        if (current < 0)
            current = 0;
        update(String.valueOf(current), DELAY_TO_SEND_COMMAND);
    }

    @OnClick(B.id.buttonPlus)
    public void onPlusClick(View v) {
        defaultBlockResetUpdate();
        current += 1;
        if (current > 100)
            current = 100;
        update(String.valueOf(current), DELAY_TO_SEND_COMMAND);
    }

    protected static class Queuer extends Handler {
        static final int SEND_COMMAND = 1;
        private final WeakReference<ViewController> weakControllerLevel;

        public Queuer(ViewController viewControllerLevel) {
            weakControllerLevel = new WeakReference<ViewController>(viewControllerLevel);
        }

        public void sendCommand(final UUID uuid, final String value, final long delay) {
            sendCommand(new DataObject(uuid, value), delay);
        }

        public void sendCommand(final DataObject dataObject, final long delay) {
            removeMessages(SEND_COMMAND);
            sendMessageDelayed(obtainMessage(SEND_COMMAND, dataObject), delay);
        }

        public void sendCommand(final long delay, final DataObject... dataObjects) {
            removeMessages(SEND_COMMAND);
            for (DataObject dataObject : dataObjects) {
                sendMessageDelayed(obtainMessage(SEND_COMMAND, dataObject), delay);
            }
        }

        @Override
        public void handleMessage(Message msg) {
            if ((msg.what == SEND_COMMAND) && (weakControllerLevel.get() != null)) {
                final DataObject dataObject = (DataObject) msg.obj;
                if (dataObject != null) {
                    final ViewController absLevelController = weakControllerLevel.get();
                    if (absLevelController == null) {
                        Log.e("Queuer", String.format("null absLevelController on @ %s returning", "handleMessage method"));
                        return;
                    }
                    absLevelController.sendAttributeValue(dataObject.uuid, dataObject.value);
                }
            } else
                super.handleMessage(msg);

        }

        static class DataObject {
            UUID uuid;
            String value;

            public DataObject(UUID uuid, String value) {
                this.uuid = uuid;
                this.value = value;
            }
        }
    }


}
