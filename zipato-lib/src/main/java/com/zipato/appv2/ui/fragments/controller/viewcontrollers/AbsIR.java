/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

package com.zipato.appv2.ui.fragments.controller.viewcontrollers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.zipato.annotation.SetTypeFace;
import com.zipato.appv2.R.color;
import com.zipato.appv2.R.id;
import com.zipato.appv2.ui.fragments.adapters.controllers.GenericAdapter;
import com.zipato.appv2.ui.fragments.controller.ViewControllerLogic;
import com.zipato.appv2.ui.fragments.vcmenu.BaseTypesFragment;
import com.zipato.model.endpoint.ClusterEndpoint;
import com.zipato.model.typereport.EntityType;
import com.zipato.model.typereport.TypeReportItem;
import com.zipato.util.TagFactoryUtils;
import com.zipato.v2.client.ApiV2RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

import static com.zipato.util.Utils.capitalizer;

/**
 * Created by murielK on 8/19/2015.
 */
public abstract class AbsIR extends AbsPagePointerAdapter implements ViewControllerLogic {

    static final int MODE_NORMAL = 0;
    static final int MODE_EDIT = 1;
    private static final String TAG = TagFactoryUtils.getTag(AbsIR.class);
    private static final String MODE_KEY = "MODE_KEY";
    private final Animation[] editAnimation = new Animation[1];
    protected int mode;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewStatus)
    TextView textViewAtrName;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.textViewValue)
    TextView textViewValue;
    @SetTypeFace("helveticaneue_ultra_light.otf")
    @InjectView(id.butIREdit)
    TextView edit;
    @Inject
    ApiV2RestTemplate restTemplate;
    @Inject
    ExecutorService executor;
    private int logicQueueID;

    public AbsIR(View itemView, RecyclerView recyclerView) {
        super(itemView, recyclerView);
        Animation animation = new AlphaAnimation(0.3f, 1.0f);
        animation.setDuration(700);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(-1);
        editAnimation[0] = animation;
    }

    @Override
    public Animation[] getOnNextAnimation() {
        if (mode == MODE_EDIT)
            return editAnimation;
        Animation anim0 = new AlphaAnimation(0.0f, 1.0f); // at least it wont be stored in the ram until this is killed!
        anim0.setDuration(450);
        Animation anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(450);
        anim1.setStartOffset(150);
        Animation anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(450);
        anim2.setStartOffset(250);
        Animation anim3 = new AlphaAnimation(0.0f, 1.0f);
        anim3.setDuration(350);
        anim3.setStartOffset(350);
        Animation anim4 = new AlphaAnimation(0.0f, 1.0f);
        anim4.setDuration(300);
        anim4.setStartOffset(400);
        Animation anim5 = new AlphaAnimation(0.0f, 1.0f);
        anim5.setDuration(250);
        anim5.setStartOffset(500);
        return new Animation[]{anim0, anim1, anim2, anim3, anim4, anim5};
    }

    @Override
    public Animation[] getOnPrevAnimation() {
        if (mode == MODE_EDIT)
            return editAnimation;
        Animation anim0 = new AlphaAnimation(0.0f, 1.0f); // at least it wont be stored in the ram until this is killed!
        anim0.setDuration(450);
        Animation anim1 = new AlphaAnimation(0.0f, 1.0f);
        anim1.setDuration(450);
        anim1.setStartOffset(150);
        Animation anim2 = new AlphaAnimation(0.0f, 1.0f);
        anim2.setDuration(450);
        anim2.setStartOffset(250);
        Animation anim3 = new AlphaAnimation(0.0f, 1.0f);
        anim3.setDuration(350);
        anim3.setStartOffset(350);
        Animation anim4 = new AlphaAnimation(0.0f, 1.0f);
        anim4.setDuration(300);
        anim4.setStartOffset(400);
        Animation anim5 = new AlphaAnimation(0.0f, 1.0f);
        anim5.setDuration(250);
        anim5.setStartOffset(500);
        return new Animation[]{anim5, anim4, anim3, anim2, anim1, anim0};
    }

    @Override
    public Animation[] getOnBindAnimation() {
        return (mode == MODE_EDIT) ? editAnimation : null;
    }

    @Override
    public int getPointerCount() {
        return isClusterValid() ? getCluster().getConfig().getSlots() : 0;
    }

    @Override
    public String getLabelForPointer(int pointer) {
        if (isPointerEnable(pointer))
            return getCluster().getConfig().getSlotNames()[pointer - 1];
        return String.valueOf(pointer);
    }

    @Override
    public boolean isPointerEnable(int pointer) {
        final int indexOfPointer = pointer - 1;
        return isClusterValid() && ((getCluster().getConfig().getSlotNames() != null)
                && (getCluster().getConfig().getSlotNames().length > indexOfPointer))
                && (getCluster().getConfig().getSlotNames()[indexOfPointer] != null);
    }

    @Override
    public void handleViewClick(TextView textView) {
        if (!isClusterValid())
            return;

        final ClusterEndpoint clusterEndpoint = getCluster();

        final int pointer = getPointFromTextView(textView);

        String command = null;
        try {
            command = clusterEndpoint.getConfig().getSlotNames()[pointer - 1];
        } catch (Exception e) {
            Log.d(TAG, "", e);
        }

        switch (mode) {
            case MODE_NORMAL:
                invokeCommand(pointer, command); // to point the actual command number
                break;
            case MODE_EDIT:
                onEdit(pointer, command);// to point the actual command number
                break;
        }
    }

    @Override
    public void handleLongClick(TextView textView) {
        if (!isClusterValid())
            return;

        final ClusterEndpoint clusterEndpoint = getCluster();

        final int pointer = getPointFromTextView(textView);

        try {
            if (clusterEndpoint.getConfig().getSlotNames()[pointer - 1] != null)
                seeCommand(clusterEndpoint.getConfig().getSlotNames()[pointer - 1]);
            else
                seeCommand("-");
        } catch (Exception e) {
            Log.d(TAG, "", e);
            seeCommand("-");
        }

    }

    @Override
    public void onPreBind(TypeReportItem item) {
        final Object cache = getValueFromVCCache(item.getKey(), MODE_KEY);
        if (cache != null)
            mode = (Integer) cache;
        refreshTextViews();
    }

    @Override
    protected void init() {
        super.init();
        edit.setText(capitalizer(languageManager.translate("edit")));
        textViewValue.setText("-");
    }

    public abstract int provideViewTypeID();

    public abstract String provideLearnAction();

    public abstract String provideLearnName();

    public abstract String provideReceivedCommand();


    @OnClick(id.butIREdit)
    public void onEditClick(View v) {
        delayedUpdates();
        if (mode == MODE_NORMAL) {
            mode = MODE_EDIT;
        } else {
            mode = MODE_NORMAL;
        }

        final TypeReportItem item = getTypeReportItem();
        if (item == null)
            return;
        putToVCCache(item.getKey(), MODE_KEY, mode);
        refreshIndex();
    }


    private void refreshIndex() {
        refreshTextViews();
        switch (mode) {
            case MODE_NORMAL:
                refreshNormalMode();
                break;
            case MODE_EDIT:
                refreshEditMode();
                break;
        }
    }

    private void refreshTextViews() {
        if (!isClusterValid())
        {   textViewAtrName.setText(capitalizer(languageManager.translate("loading_box")));
            textViewValue.setText("");
            edit.setText("...");
            return;
        }

        switch (mode) {
            case MODE_NORMAL:
                edit.setText(capitalizer(languageManager.translate("edit")));
                textViewAtrName.setText(capitalizer(languageManager.translate("command")));
                textViewValue.setText("");
                break;
            case MODE_EDIT:
                edit.setText(capitalizer(languageManager.translate("exit")));
                textViewAtrName.setText(capitalizer(languageManager.translate("edit_mode")));
                textViewValue.setText("");
                break;
        }
    }

    private void refreshNormalMode() {
        manualRefresh(null);
    }

    private void refreshEditMode() {

        manualRefresh(editAnimation);
    }


    @Override
    public void setLogicQueueID(int logicQueueID) {
        this.logicQueueID = logicQueueID;
    }

    @Override
    public void run() {
        final ThreadLocal<Integer> localLogicID = new ThreadLocal<>();
        localLogicID.set(logicQueueID);
        final int viewTye = provideViewTypeID();
        final GenericAdapter genericAdapter = getAdapter();
        boolean isSuccess = true;
        try {
            final int itemCount = ((Adapter) genericAdapter).getItemCount();
            for (int i = 0; i < itemCount; i++) {
                final TypeReportItem typeReportItem = genericAdapter.getTypeReportItem(i);
                if ((typeReportItem == null) || (typeReportItem.getEntityType() != EntityType.CLUSTER_ENDPOINT))
                    continue;
                final ClusterEndpoint cepIn = clusterEndpointRepository.get(typeReportItem.getUuid());
//                if (cepIn.getConfig() !=null)
//                    continue;
                final ClusterEndpoint cep = restTemplate.getForObject("v2/clusterEndpoints/{uuid}?config=true", ClusterEndpoint.class, typeReportItem.getUuid());
                cepIn.setConfig(cep.getConfig());
            }
        } catch (Exception e) {
            Log.d(TAG, "", e);
            isSuccess = false;
        } finally {
            if (genericAdapter != null) {
                if (isSuccess) genericAdapter.logicExecuted(viewTye, true, localLogicID.get());
                else genericAdapter.logicFailExecution(viewTye, localLogicID.get());
            }
        }
    }

    protected ClusterEndpoint getCluster() {
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.e(TAG, String.format("%s null on %s method call", "item", "getCluster"));
            return null;
        }
        final ClusterEndpoint clusterEndpoint = clusterEndpointRepository.get(item.getUuid());
        if (clusterEndpoint == null) {
            Log.e(TAG, String.format("%s null on %s method call", "clusterEndpoint", "getCluster"));
            return null;
        }

        return clusterEndpoint;
    }

    private void invokeCommand(final int pointer, final String command) {
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            return;
        }

        sendAttributeValue(item.getAttrOfID(BaseTypesFragment.VALUE).getUuid(), String.valueOf(pointer));
        if (command != null)
            displaySentCommand(command);
        else
            displaySentCommand("-");
    }


    private void save(final String value, final int pointer) {
        if (!isClusterValid())
            return;
        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on save method");
            return;
        }
        final ClusterEndpoint clusterEndpoint = getCluster();
        final Handler handler = getHandler();

        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage(languageManager.translate("saving_configurations"));
        progressDialog.setCancelable(false);
        progressDialog.show();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                Map<String, Object> configObject = new HashMap<>();
                try {
                    String[] slotNames = clusterEndpoint.getConfig().getSlotNames();
                    if (slotNames == null)
                        slotNames = new String[clusterEndpoint.getConfig().getSlots()];
                    slotNames[pointer - 1] = value;
                    configObject.put("slotNames", slotNames);
                    restTemplate.put("v2/clusterEndpoints/{uuid}/config", configObject, clusterEndpoint.getUuid());
                    final ClusterEndpoint cepIn = restTemplate.getForObject("v2/clusterEndpoints/{uuid}?config=true", ClusterEndpoint.class, clusterEndpoint.getUuid());
                    clusterEndpoint.setConfig(cepIn.getConfig());

                } catch (Exception e) {
                    success = false;
                } finally {
                    final boolean finalSuccess = success;
                    if ((handler != null))
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.dismiss();
                                if (!finalSuccess)
                                    Toast.makeText(context, languageManager.translate("saving_config_fail"), Toast.LENGTH_LONG).show();
                            }
                        });
                }
            }
        });
    }

    protected void onEdit(final int pointer, final CharSequence command) {
        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on onEdit method");
            return;
        }
        final DialogViewHolder dialogViewHolder = new DialogViewHolder(context);
        dialogViewHolder.getEditText().setTextColor(context.getResources().getColor(color.color_white));
        if (command != null)
            dialogViewHolder.getEditText().setHint(command);
        else

            dialogViewHolder.getEditText().setHint(languageManager.translate("enter_title"));

        Builder builder = new Builder(context);
        builder.setView(dialogViewHolder.getLinearLayout());

        if (command != null) builder.setTitle(languageManager.translate("title") + ": " + command);
        else builder.setTitle(languageManager.translate("title"));

        builder.setPositiveButton(languageManager.translate("save"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setNeutralButton(languageManager.translate("learn"), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialogViewHolder.getEditText().getText() != null) {
                    save(dialogViewHolder.getEditText().getText().toString(), pointer);
                    dialog.dismiss();
                } else {
                    Toast.makeText(context, languageManager.translate("please_enter_title"), Toast.LENGTH_LONG).show();
                }

            }
        });
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                learn(pointer);
            }
        });
    }

    private void learn(final int pointer) {
        final TypeReportItem item = getTypeReportItem();
        if (item == null) {
            Log.e(TAG, String.format("%s null on %s method call", "item", "learn"));
            return;
        }
        final Context context = getContext();
        if (context == null) {
            Log.d(TAG, "Freaking null context on at on learn method");
            return;
        }
        final Handler handler = getHandler();
        Toast.makeText(context, languageManager.translate("command_learn_sent"), Toast.LENGTH_LONG).show();//"command_learn_sent"
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final String action = provideLearnAction();
                    final String fieldName = provideLearnName();
                    Map<String, Object> object = new HashMap<>();
                    object.put(fieldName, pointer);

                    restTemplate.postForObject("v2/clusterEndpoints/{uuid}/actions/{leanAction}", object, HashMap.class, item.getUuid(), action);
                    Log.d(TAG, String.format("learning name: %d", pointer));
                    if ((provideReceivedCommand() != null) && (handler != null))
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, languageManager.translate(provideReceivedCommand()), Toast.LENGTH_LONG).show();
                            }
                        });
                } catch (Exception e) {
                    //Empty
                }
            }
        });

    }

    private void seeCommand(final String command) {
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(200); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(7);
        anim.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                textViewValue.setText("");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        final Animation animation = textViewValue.getAnimation();
//        if (animation != null)
//            animation.cancel();
        textViewValue.setAnimation(null);
        textViewValue.setText(command);
        textViewValue.startAnimation(anim);
    }

    private void displaySentCommand(String command) {

        Animation animIn = new AlphaAnimation(0.0f, 1.0f);
        animIn.setDuration(300); //You can manage the blinking time with this parameter
        animIn.setStartOffset(10);
        animIn.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Animation animOut = new AlphaAnimation(1.0f, 0.0f);
                animOut.setDuration(450); //You can manage the blinking time with this parameter
                animOut.setStartOffset(1000);
                animOut.setAnimationListener(new AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                        textViewValue.setText("");
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
//                final Animation animOld = textViewValue.getAnimation();
//                if (animOld != null)
//                    animOld.cancel();
                textViewValue.setAnimation(null);
                textViewValue.startAnimation(animOut);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
//        final Animation animOld = textViewValue.getAnimation();
//        if (animOld != null)
//            animOld.cancel();
        textViewValue.setAnimation(null);
        textViewValue.setText(command);
        textViewValue.startAnimation(animIn);

    }

    protected boolean isClusterValid() {
        return (getCluster() != null) && (getCluster().getConfig() != null);
    }

    @Override
    public boolean hasLogic() {
        return true;
    }


    static final class DialogViewHolder {

        private final Context context;
        private EditText editText;
        private LinearLayout linearLayout;

        public DialogViewHolder(Context context) {
            this.context = context;
        }


        public void build() {
            final float scale = context.getResources().getDisplayMetrics().density;
            int dpAsPixels = (int) ((10 * scale) + 0.5f);
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params.bottomMargin = dpAsPixels;
            params.topMargin = dpAsPixels;
            editText = new EditText(context);
            editText.setLayoutParams(params);
            editText.setGravity(Gravity.CENTER);
            linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.addView(editText);
        }

        public LinearLayout getLinearLayout() {
            if (linearLayout == null)
                build();
            return linearLayout;
        }

        public EditText getEditText() {
            if (editText == null)
                build();
            return editText;
        }

    }

}
