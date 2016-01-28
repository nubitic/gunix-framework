package mx.com.gunix.framework.ui.vaadin.view;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mx.com.gunix.framework.processes.domain.ProgressUpdate;
import mx.com.gunix.framework.processes.domain.Variable;
import mx.com.gunix.framework.ui.vaadin.spring.GunixVaadinView;

import org.springframework.util.StringUtils;

import rx.Observable;
import rx.Scheduler;
import rx.schedulers.Schedulers;

import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@GunixVaadinView
public final class GunixProgressMonitorView<S extends Serializable> extends AbstractGunixView<S> {
	private static final long serialVersionUID = 1L;
    private final SimpleDateFormat sdf = new SimpleDateFormat("[HH:mm:ss] ");
	private VerticalLayout layout;
	private ProgressBar bar;
	private TextArea log;
	private Button continuarButton;
	private Window window;
	private Observable<ProgressUpdate> messagePoller;
	private Scheduler scheduler;
	
	protected void doEnter(ViewChangeEvent event) {

	}

	protected void doConstruct() {
		layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		bar = new ProgressBar(0.0f);
		bar.setWidth("100%");
		layout.addComponent(bar);

		log = new TextArea("Mensajes");
		log.setWordwrap(false);
		log.setRows(15);
		log.setColumns(75);
		log.setReadOnly(true);
		layout.addComponent(log);

		continuarButton = new Button("Continuar");
		continuarButton.addClickListener(cliclEvnt -> {
			completaTarea();
			window.close();
		});
		continuarButton.setVisible(false);
		continuarButton.setDisableOnClick(true);
		layout.addComponent(continuarButton);
		layout.setComponentAlignment(continuarButton, Alignment.BOTTOM_RIGHT);

		window = new Window("Estado del proceso");
		window.setModal(true);
		window.setClosable(false);
		window.setResizable(false);
		window.setWidth("1073px");
		window.setHeight("465px");
		window.center();
		window.setContent(layout);
		UI.getCurrent().addWindow(window);
		
		(messagePoller = Observable.interval(4, TimeUnit.SECONDS, scheduler = Schedulers.io())
			      .map(tick -> as.getRecentProgressUpdates(getTarea().getInstancia().getId()))
				  .doOnError(err -> UI.getCurrent().access(() -> {
							  Notification.show("Hubo un error al obtener el estado del proceso:\n" + err, Type.ERROR_MESSAGE);
							  window.close();
							  finishObservable();
						  }))
			      .flatMap(Observable::from)
			      .distinct())
			      .subscribe(pu -> UI.getCurrent().access(() -> {
			    	  if(pu.isCancelado()) {
			    		  UI.getCurrent().access(() -> {
							  Notification.show("El proceso terminó abruptamente con el siguiente mensaje:\n" + pu.getMensaje(), Type.ERROR_MESSAGE);
							  window.close();
							  finishObservable();
						  });
			    	  } else {
				    	  if(!StringUtils.isEmpty(pu.getMensaje())) {
				    		  StringBuilder nMss = new StringBuilder(sdf.format(new Date(pu.getTimeStamp())));
					    	  nMss.append(pu.getMensaje());
					    	  addLog(nMss.toString());
				    	  }
				    	  bar.setValue(pu.getProgreso());
				    	  if(pu.getProgreso()==1f) {
				    		  continuarButton.setVisible(true);
				    		  finishObservable();
				    	  }
			    	  }
				  }));
	}

	private void finishObservable() {
		messagePoller.takeUntil(obj -> (true));
		messagePoller.unsubscribeOn(scheduler);
	}

	private void addLog(String mensaje) {
		StringBuilder newValue = new StringBuilder(log.getValue());
		newValue.append(mensaje);
		newValue.append("\n");
		log.setReadOnly(false);
		log.setValue(newValue.toString());
		log.setCursorPosition(log.getValue().length());
		log.setReadOnly(true);
	}

	protected List<Variable<?>> getVariablesTarea() {
		return null;
	}

	protected String getComentarioTarea() {
		return null;
	};
}
