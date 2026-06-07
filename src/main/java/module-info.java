module Rulength {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.swing;
    requires java.desktop;
    requires com.fasterxml.jackson.databind;
    requires java.desktop;

    exports event;
    exports event.input;
    exports game;
    exports game.slice;
    exports game.time;
    exports game.window;
    exports config;
    exports util;

    exports game.dialog;
    opens game.dialog to com.fasterxml.jackson.databind;

    opens game to javafx.graphics;
    opens game.window to javafx.graphics;
    opens game.slice to javafx.graphics;
    opens config to com.fasterxml.jackson.databind;
    opens event to javafx.graphics;
    opens event.input to javafx.graphics;
    opens util to javafx.graphics;
}
