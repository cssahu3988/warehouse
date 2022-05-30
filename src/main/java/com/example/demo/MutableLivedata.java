package com.example.demo;

public class MutableLivedata<T> {
    private T t = null;
    private onChangedListener onChangedListener;

    public MutableLivedata(T t) {
        this.t = t;
    }

    public MutableLivedata() {

    }

    public void observe(onChangedListener onChangedListener){
        this.onChangedListener = onChangedListener;
        setT(t);
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
        if (this.onChangedListener==null){
            return;
        }
        this.onChangedListener.onChanged(t);
    }

    interface onChangedListener{
        void onChanged(Object ob);
    }

}
