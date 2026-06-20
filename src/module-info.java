module Game {
    requires javafx.controls;
    requires javafx.graphics;

    opens core to javafx.graphics;
}
