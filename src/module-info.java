module Game {
    requires javafx.controls;
    requires javafx.graphics;
	requires javafx.media;

    opens core to javafx.graphics;
}
