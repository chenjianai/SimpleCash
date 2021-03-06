package com.big.simplecash.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.big.simplecash.Application;
import com.big.simplecash.BaseActivity;
import com.big.simplecash.R;
import com.big.simplecash.settle.SettlementActivity;
import com.big.simplecash.SingleEditDialog;
import com.big.simplecash.greendao.GreenDaoUtils;
import com.big.simplecash.greendao.Order;
import com.big.simplecash.greendao.SaleInfo;
import com.big.simplecash.material.MaterialActivity;
import com.big.simplecash.util.CallBack;
import com.big.simplecash.util.SharePrefer;
import com.big.simplecash.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by big on 2019/6/11.
 */

public class SaleActivity extends BaseActivity implements
        View.OnClickListener {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;
    List<SaleInfo> mList = new ArrayList<>();
    private TextView mSum;
    private TextView mRate, mCost, mTransIn, mTransOut, mDiscount, mSave;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale);
        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.settle).setOnClickListener(this);
        mSave = (TextView) findViewById(R.id.save);
        mRate = (TextView) findViewById(R.id.rate_content);
        mCost = (TextView) findViewById(R.id.cost_content);
        mSum = (TextView) findViewById(R.id.sum_content);
        mTransIn = (TextView) findViewById(R.id.trans_in_content);
        mTransOut = (TextView) findViewById(R.id.trans_out_content);
        mDiscount = (TextView) findViewById(R.id.discount_content);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mSave.setOnClickListener(this);
        mRate.setOnClickListener(this);
        mCost.setOnClickListener(this);
        mTransIn.setOnClickListener(this);
        mTransOut.setOnClickListener(this);
        mDiscount.setOnClickListener(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);
        if (getIntent().hasExtra("data")) {
            mOrder = (Order) getIntent().getSerializableExtra("data");
            readOrder();
        }
    }

    private void readOrder() {
        mOrder.parseList(mList);
        mRate.setText(mOrder.rate + "");
        mCost.setText(mOrder.cost + "");
        mSum.setText(String.format("%.1f", mOrder.totalPurchase));
        mTransIn.setText(Utils.getText(mOrder.transIn));
        mTransOut.setText(Utils.getText(mOrder.transOut));
        mDiscount.setText(Utils.getText(mOrder.discount));
        mAdapter.notifyDataSetChanged();
    }

    private boolean mIsEditMode;

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.save) {
            if (mIsEditMode && save()) {
                Toast.makeText(this, "订单保存成功", Toast.LENGTH_LONG).show();
                mIsEditMode = false;
            } else {
                mIsEditMode = true;
            }
            mSave.setText(mIsEditMode ? R.string.save : R.string.edit);
        } else if (view.getId() == R.id.settle) {
            if (save()) {
                Intent intent = new Intent(this, SettlementActivity.class);
                intent.putExtra("data", mOrder);
                startActivity(intent);
                GreenDaoUtils.insertSettle(mOrder);
                GreenDaoUtils.deleteOrder(mOrder);
                finish();
            }
        } else if (view.getId() == R.id.settle) {
            Intent intent = new Intent(this, MaterialActivity.class);
            intent.putExtra("from", "sale");
            startActivityForResult(intent, 101);
        } else if (view.getId() == R.id.add) {
            Intent intent = new Intent(this, MaterialActivity.class);
            intent.putExtra("from", "sale");
            startActivityForResult(intent, 101);
        } else if (mIsEditMode) {
            final TextView inputTxt = (TextView) view;
            String name = "";
            if (view.getId() == R.id.rate_content) {
                name = "汇率";
            } else if (view.getId() == R.id.cost_content) {
                name = "其他成本￥";
            } else if (view.getId() == R.id.trans_in_content) {
                name = "运费收入￥";
            } else if (view.getId() == R.id.trans_out_content) {
                name = "运费支出￥";
            } else if (view.getId() == R.id.discount_content) {
                name = "优惠HK$";
            }
            showSingleDialog(inputTxt, name, String.valueOf(inputTxt.getText()));
        }

    }

    private Order mOrder;

    private boolean save() {
        if (TextUtils.isEmpty(mRate.getText()) || mList.size() <= 0) {
            Toast.makeText(SaleActivity.this, "汇率或订单为空", Toast.LENGTH_LONG).show();
            return false;
        } else if (TextUtils.isEmpty(mCost.getText())) {
            Toast.makeText(SaleActivity.this, "其他成本为空", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (mOrder == null) {
                mOrder = new Order();
                mOrder.createDate = System.currentTimeMillis();
                mOrder.name = Application.mNameDateFormat.format(new Date(mOrder.createDate))
                        + "-" + SharePrefer.getDateNum(mOrder.createDate);
                mOrder.modifyTime = mOrder.createDate;
            }
            mOrder.createContent(mList);
            mOrder.totalPurchase = Float.parseFloat(String.valueOf(mSum.getText()));
            mOrder.rate = Float.parseFloat(String.valueOf(mRate.getText()));
            mOrder.cost = Float.parseFloat(String.valueOf(mCost.getText()));

            mOrder.transIn = Utils.getTextFloat(mTransIn);
            mOrder.transOut = Utils.getTextFloat(mTransOut);
            mOrder.discount = Utils.getTextFloat(mDiscount);
            GreenDaoUtils.insertOrder(mOrder);
            return true;
        }
    }

    private void sum() {
        float sum = 0;
        for (SaleInfo info : mList) {
            if (info.price == 0) continue;
            sum += info.realPrice * info.number;
        }
        mSum.setText(String.format("%.1f", sum));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 101 && resultCode == 100) {
            SaleInfo info = new SaleInfo();
            info.name = Application.mTempInfo.name;
            info.price = Application.mTempInfo.price;
            info.realPrice = info.price;
            info.size = Application.mTempInfo.size;
            info.provider = Application.mTempInfo.provider;
            mList.add(info);
            Log.d("big", "add");
            mAdapter.notifyDataSetChanged();
            mIsEditMode = true;
            mSave.setText(R.string.save);
            sum();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {


        @Override
        public MyAdapter.MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View content = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
            return new MyAdapter.MyHolder(content);
        }

        @Override
        public void onBindViewHolder(MyAdapter.MyHolder holder, int position) {
            SaleInfo saleInfo = mList.get(position);
            if (position == mCurPos) {
                holder.itemView.setBackgroundColor(getResources().getColor(R.color.itemFocus));
            } else if (position % 2 != 0) {
                holder.itemView.setBackgroundColor(0xffffffff);
            } else {
                holder.itemView.setBackgroundColor(0xffdddddd);
            }
            holder.name.setText(saleInfo.name);
            holder.size.setText(saleInfo.size);
            holder.price.setText(String.valueOf(saleInfo.price));
            holder.provide.setText(saleInfo.provider);
            holder.realPrice.setText(Utils.getText(saleInfo.realPrice));
            holder.num.setText(saleInfo.number + "");
            holder.total.setText(saleInfo.realPrice * saleInfo.number + "");

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return R.layout.item_sale;
        }

        private int mCurPos = -1;

        class MyHolder extends RecyclerView.ViewHolder {
            TextView name, price, provide, size, del, total, realPrice, num;

            public MyHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.item_name);
                size = (TextView) itemView.findViewById(R.id.item_size);
                price = (TextView) itemView.findViewById(R.id.item_price);
                provide = (TextView) itemView.findViewById(R.id.item_provider);
                del = (TextView) itemView.findViewById(R.id.item_del);
                realPrice = (TextView) itemView.findViewById(R.id.item_real_price);
                num = (TextView) itemView.findViewById(R.id.item_num);
                total = (TextView) itemView.findViewById(R.id.item_total);
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (del != null) {
                            del.setVisibility(View.VISIBLE);
                        }
                        return true;
                    }
                });

                if (del != null) {
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            notifyItemChanged(mCurPos);
                            mCurPos = getAdapterPosition();
                            notifyItemChanged(mCurPos);
                            if (del.getVisibility() == View.VISIBLE) {
                                if (del != null) {
                                    del.setVisibility(View.GONE);
                                }
                            }
                            if (mIsEditMode && mCurPos >= 0 && mCurPos < mList.size()) {
                                showSaleEditDialog(mList.get(mCurPos));
                            }
                        }
                    });
                    del.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            int position = getAdapterPosition();
                            if (position >= 0 && position < mList.size()) {
                                del.setVisibility(View.GONE);
                                mList.remove(position);
                                sum();
                                notifyDataSetChanged();
                            }
                        }
                    });
                }

            }

        }
    }

    private SaleEditDialog mSaleEditDialog;

    private void showSaleEditDialog(SaleInfo info) {
        if (mSaleEditDialog == null) {
            mSaleEditDialog = new SaleEditDialog(this);
            mSaleEditDialog.setCallback(new CallBack<SaleInfo>() {
                @Override
                public void onCallBack(SaleInfo saleInfo) {
                    sum();
                    mAdapter.notifyItemChanged(mList.indexOf(saleInfo));
                }
            });
        }
        mSaleEditDialog.show(info);
    }

    private SingleEditDialog mSingleEditDialog;

    private void showSingleDialog(TextView textView, String name, String value) {
        if (mSingleEditDialog == null) {
            mSingleEditDialog = new SingleEditDialog(this);
        }
        mSingleEditDialog.show(textView, name, value);
    }
}
