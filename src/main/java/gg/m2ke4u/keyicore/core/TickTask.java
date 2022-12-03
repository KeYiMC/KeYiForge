package gg.m2ke4u.keyicore.core;

public interface TickTask<T>{
    public void call(T input);
    public boolean finished();
    public boolean terminate();
    public void forceTerminate();
}
