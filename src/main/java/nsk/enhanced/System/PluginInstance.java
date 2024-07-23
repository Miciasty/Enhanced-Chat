package nsk.enhanced.System;

import nsk.enhanced.EnhancedChat;

public class PluginInstance {


    private static EnhancedChat instance;
    public static EnhancedChat getInstance() {
        return instance;
    }
    public static void setInstance(EnhancedChat in) {
        instance = in;
    }

}
