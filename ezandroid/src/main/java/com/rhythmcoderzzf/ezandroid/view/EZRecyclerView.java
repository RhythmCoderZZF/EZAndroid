package com.rhythmcoderzzf.ezandroid.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rhythmcoderzzf.ezandroid.core.AbstractBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * EZ RecyclerView。@param <T>——ItemView绑定数据类型。
 * <p>使用方式</p>
 * <pre>
 *  EZRecyclerView mEzRecyclerView = new EZRecyclerView.Builder<String>(this)
 *      .setCallBack(new UnBoundedViewCallBack())
 *      .setRecyclerView(mBinding.rvUnBoundedDevices)
 *      .build();
 *
 *  mEzRecyclerView.setDataList(mUnBondedDevices);
 * </pre>
 */
public class EZRecyclerView<T> {
    public RecyclerView mRecyclerView;
    public RecyclerView.LayoutManager mLayoutManager;
    public EZAdapter mAdapter;
    private EZRecyclerViewCallBack mCallBack;
    private boolean mItemClickable = true;
    private List<T> mDataList = new ArrayList<>();

    private EZRecyclerView() {
    }

    private EZRecyclerView onInit() {
        mAdapter = new EZAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);
        return this;
    }

    public void setDataList(List<T> dataList) {
        if (dataList == null) {
            throw new IllegalArgumentException();
        }
        mDataList = dataList;
        mAdapter.notifyDataSetChanged();
    }

    private class EZAdapter extends RecyclerView.Adapter<EZAdapter.EZViewHolder> {
        @NonNull
        @Override
        public EZViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(mCallBack.setItemViewLayoutResId(), parent, false);
            return new EZViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }

        @Override
        public void onBindViewHolder(@NonNull EZViewHolder holder, int position) {
            mCallBack.bindViewHolder(holder.mRoot, position);
        }

        private class EZViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            private View mRoot;

            public EZViewHolder(@NonNull View itemView) {
                super(itemView);
                mRoot = itemView;
                if (mItemClickable) {
                    itemView.setOnClickListener(this);
                }
            }

            @Override
            public void onClick(View v) {
                mCallBack.onItemClick(mRoot, getAdapterPosition());
            }
        }
    }

    public static abstract class EZRecyclerViewCallBack {
        protected abstract int setItemViewLayoutResId();

        protected abstract void bindViewHolder(View itemView, int position);

        protected void onItemClick(View itemView, int position) {
        }
    }

    public static class Builder<T> implements AbstractBuilder<EZRecyclerView> {
        private Context context;
        private EZRecyclerViewCallBack callBack;
        private boolean click = true;
        private List<T> dataList;
        private RecyclerView recyclerView;
        private RecyclerView.LayoutManager layoutManager;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setCallBack(EZRecyclerViewCallBack callBack) {
            this.callBack = callBack;
            return this;
        }

        public Builder setDataList(List<T> dataList) {
            this.dataList = dataList;
            return this;
        }

        public Builder setRecyclerView(RecyclerView recyclerView) {
            this.recyclerView = recyclerView;
            return this;
        }

        public Builder setLayoutManager(RecyclerView.LayoutManager layoutManager) {
            this.layoutManager = layoutManager;
            return this;
        }

        public Builder applyItemClick(boolean click) {
            this.click = click;
            return this;
        }

        @Override
        public EZRecyclerView build() throws Exception {
            if (callBack == null || recyclerView == null) {
                throw new IllegalStateException();
            }
            EZRecyclerView ezRecyclerView = new EZRecyclerView();
            ezRecyclerView.mCallBack = callBack;
            ezRecyclerView.mRecyclerView = recyclerView;
            ezRecyclerView.mItemClickable = click;
            if (dataList != null) {
                ezRecyclerView.mDataList = dataList;
            }
            if (layoutManager != null) {
                ezRecyclerView.mLayoutManager = layoutManager;
            } else {
                ezRecyclerView.mLayoutManager = new LinearLayoutManager(context);
            }
            return ezRecyclerView.onInit();
        }
    }
}
