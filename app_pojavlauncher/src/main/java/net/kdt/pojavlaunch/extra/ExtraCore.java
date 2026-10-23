package net.kdt.pojavlaunch.extra;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Class providing callback across all of a program
 * to allow easy thread safe implementations of UI update without context leak
 * It is also perfectly engineered to make it unpleasant to use.
 *
 * This class uses a singleton pattern to simplify access to it
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public final class ExtraCore {

    private ExtraCore(){}

    private static volatile ExtraCore sExtraCoreSingleton = null;

    private final Map<String, Object> mValueMap = new ConcurrentHashMap<>();

    private final Map<String, ConcurrentLinkedQueue<WeakReference<ExtraListener>>> mListenerMap = new ConcurrentHashMap<>();

    private static ExtraCore getInstance(){
        if(sExtraCoreSingleton == null){
            synchronized(ExtraCore.class){
                if(sExtraCoreSingleton == null){
                    sExtraCoreSingleton = new ExtraCore();
                }
            }
        }
        return sExtraCoreSingleton;
    }

    /**
     * Set the value associated to a key and trigger all listeners
     * @param key The key
     * @param value The value
     */
    public static void setValue(String key, Object value){
        if(value == null || key == null) return;

        getInstance().mValueMap.put(key, value);
        ConcurrentLinkedQueue<WeakReference<ExtraListener>> extraListenerList = getInstance().mListenerMap.get(key);
        if(extraListenerList == null) return;
        for(WeakReference<ExtraListener> listener : extraListenerList){
            if(listener.get() == null){
                extraListenerList.remove(listener);
                continue;
            }

            if(listener.get().onValueSet(key, value)){
                ExtraCore.removeExtraListenerFromValue(key, listener.get());
            }
        }
    }

    /** @return The value behind the key */
    public static Object getValue(String key){
        return getInstance().mValueMap.get(key);
    }

    /** @return The value behind the key, or the default value */
    public static Object getValue(String key, Object defaultValue){
        Object value = getInstance().mValueMap.get(key);
        return value != null ? value : defaultValue;
    }

    /** Remove the key and its value from the valueMap */
    public static void removeValue(String key){
        getInstance().mValueMap.remove(key);
    }

    public static Object consumeValue(String key){
        Object value = getInstance().mValueMap.get(key);
        getInstance().mValueMap.remove(key);
        return value;
    }

    /** Remove all values */
    public static void removeAllValues(){
        getInstance().mValueMap.clear();
    }

    /**
     * Link an ExtraListener to a value
     * @param key The value key to look for
     * @param listener The ExtraListener to link
     */
    public static void addExtraListener(String key, ExtraListener listener){
        ConcurrentLinkedQueue<WeakReference<ExtraListener>> listenerList = getInstance().mListenerMap.get(key);

        if(listenerList == null){
            listenerList = new ConcurrentLinkedQueue<>();
            getInstance().mListenerMap.put(key, listenerList);
        }

        listenerList.add(new WeakReference<>(listener));
    }

    /**
     * Unlink an ExtraListener from a value.
     * Unlink null references found along the way
     * @param key The value key to ignore now
     * @param listener The ExtraListener to unlink
     */
    public static void removeExtraListenerFromValue(String key, ExtraListener listener){
        ConcurrentLinkedQueue<WeakReference<ExtraListener>> listenerList = getInstance().mListenerMap.get(key);

        if(listenerList == null){
            listenerList = new ConcurrentLinkedQueue<>();
            getInstance().mListenerMap.put(key, listenerList);
        }

        for(WeakReference<ExtraListener> listenerWeakReference : listenerList){
            ExtraListener actualListener = listenerWeakReference.get();

            if(actualListener == null || actualListener == listener){
                listenerList.remove(listenerWeakReference);
            }
        }
    }

    /**
     * Unlink all ExtraListeners from a value
     * @param key The key to which ExtraListener are linked
     */
    public static void removeAllExtraListenersFromValue(String key){
        ConcurrentLinkedQueue<WeakReference<ExtraListener>> listenerList = getInstance().mListenerMap.get(key);

        if(listenerList == null){
            listenerList = new ConcurrentLinkedQueue<>();
            getInstance().mListenerMap.put(key, listenerList);
        }

        listenerList.clear();
    }

    /**
     * Remove all ExtraListeners from listening to any value
     */
    public static void removeAllExtraListeners(){
        getInstance().mListenerMap.clear();
    }

}