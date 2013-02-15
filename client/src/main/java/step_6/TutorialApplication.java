package step_6;

import com.canoo.dolphin.binding.JFXBinder;
import com.canoo.dolphin.core.PresentationModel;
import com.canoo.dolphin.core.client.ClientAttribute;
import com.canoo.dolphin.core.client.ClientDolphin;
import com.canoo.dolphin.core.client.ClientPresentationModel;
import com.canoo.dolphin.core.client.comm.OnFinishedHandlerAdapter;
import groovy.lang.Closure;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransitionBuilder;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFieldBuilder;
import javafx.scene.layout.Pane;
import javafx.scene.layout.PaneBuilder;
import javafx.scene.layout.VBoxBuilder;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

import static step_6.TutorialConstants.*;

public class TutorialApplication extends Application {


    static ClientDolphin clientDolphin;

    private TextField textField;
    private Button button;
    private Button reset;
    private PresentationModel textAttributeModel;


    public TutorialApplication() {
        textAttributeModel = clientDolphin.presentationModel(PERSON_MODEL_ID, new ClientAttribute(FIRSTNAME_ID, ""));
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = PaneBuilder.create().children(
                VBoxBuilder.create().id("content").children(
                        textField = TextFieldBuilder.create().id("firstname").build(),
                        button = ButtonBuilder.create().text("save").build(),
                        reset = ButtonBuilder.create().text("reset").build()
                ).build()
        ).build();

        addClientSideAction();
        setupBinding();

        Scene scene = new Scene(root, 300, 250);
        stage.setScene(scene);
        stage.setTitle(getClass().getName());
        scene.getStylesheets().add("/step_6/tutorial.css");

        stage.show();
    }

    private void setupBinding() {
        JFXBinder.bind("text").of(textField).to(FIRSTNAME_ID).of(textAttributeModel);
        JFXBinder.bind(FIRSTNAME_ID).of(textAttributeModel).to("text").of(textField);

        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("style").of(textField, new Closure(null) {
            public String call(Boolean dirty) {
                if (dirty) {
                    textField.getStyleClass().add("dirty");
                } else {
                    textField.getStyleClass().remove("dirty");
                }
                return "";
            }
        });
        Inverter inv = new Inverter();
        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("disabled").of(button, inv);
        JFXBinder.bindInfo("dirty").of(textAttributeModel).to("disabled").of(reset, inv);
    }

    private void addClientSideAction() {
        textField.setOnAction(new RebaseHandler(textAttributeModel));
        button.setOnAction(new RebaseHandler(textAttributeModel));
        final Transition fadeIn = RotateTransitionBuilder.create().node(textField).toAngle(0).duration(Duration.millis(200)).build();
        final Transition fadeOut = RotateTransitionBuilder.create().node(textField).fromAngle(-3).interpolator(Interpolator.LINEAR).
                toAngle(3).cycleCount(3).duration(Duration.millis(100)).
                onFinished(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        textAttributeModel.getAt(FIRSTNAME_ID).reset();
                        fadeIn.playFromStart();
                    }
                }).build();

        reset.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                fadeOut.playFromStart();
            }
        });
    }

    private static class Inverter extends Closure {
        public Inverter() {
            super(null);
        }

        protected Object call(Boolean dirtyState) {
            return !dirtyState;
        }
    }

    private static class RebaseHandler implements EventHandler {
        private PresentationModel model;

        public RebaseHandler(PresentationModel model) {
            this.model = model;
        }

        @Override
        public void handle(Event event) {
            clientDolphin.send(COMMAND_ID, new OnFinishedHandlerAdapter() {
                @Override
                public void onFinished(List<ClientPresentationModel> presentationModels) {
                    model.getAt(FIRSTNAME_ID).rebase();
                }
            });
        }

    }
}
