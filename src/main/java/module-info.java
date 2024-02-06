module cc.originx.lrd1122ss {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires java.base;
    requires kotlin.stdlib.jdk7;

    opens cc.originx.lrd1122ss to javafx.fxml;
    exports cc.originx.lrd1122ss;
}