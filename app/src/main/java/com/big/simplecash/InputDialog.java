package com.big.simplecash;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.big.simplecash.greendao.GreenDaoUtils;
import com.big.simplecash.greendao.MaterialInfo;
import com.big.simplecash.greendao.Order;
import com.big.simplecash.greendao.SaleInfo;
import com.big.simplecash.util.Base64;
import com.big.simplecash.util.CallBack;
import com.big.simplecash.util.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by big on 2019/6/12.
 */

public class InputDialog extends Dialog implements View.OnClickListener {
    public InputDialog(@NonNull Context context) {
        super(context, R.style.input_dialog);
        init();
    }

    private TextView mInput;

    private void init() {
        View contentView = View.inflate(getContext(), R.layout.dialog_input, null);
        setContentView(contentView, new WindowManager.LayoutParams(-1, -1));
        getWindow().setLayout(-1, -1);
        mInput = (TextView) contentView.findViewById(R.id.input_content);
        findViewById(R.id.confirm).setOnClickListener(this);
    }

    @Override
    public void show() {
        super.show();
        if (mInput != null) {
            mInput.setText("");
        }
    }

    CallBack<Order> mCallBack;

    public void setCallback(CallBack<Order> callback) {
        mCallBack = callback;
    }

    @Override
    public void onClick(View view) {
        dismiss();
        if (view.getId() == R.id.confirm) {
            if (!TextUtils.isEmpty(mInput.getText())) {
                final Order order = Order.intPut(Utils.unCompress(String.valueOf(mInput.getText())));
                if (mCallBack != null) {
                    mCallBack.onCallBack(order);
                    if (order != null) {
                        new Thread() {
                            @Override
                            public void run() {
                                List<SaleInfo> list = new ArrayList<>();
                                order.parseList(list);
                                for (SaleInfo info : list) {
                                    GreenDaoUtils.insertMaterialInfo(info);
                                }
                            }
                        }.start();
                    }

                }
            }
        }
    }
}
