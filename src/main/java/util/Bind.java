package util;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public interface Bind {
    static StringProperty stringBind(String ss){
        
        StringProperty s = new SimpleStringProperty(ss);
        return s;
    };
}
