package mx.com.gunix.framework.ui.component;

import java.util.List;
import java.util.Optional;

import mx.com.gunix.framework.domain.Funcion;
import mx.com.gunix.framework.domain.Modulo;
import mx.com.gunix.framework.domain.Rol;
import mx.com.gunix.framework.domain.Usuario;

import org.springframework.security.core.context.SecurityContextHolder;
import org.vaadin.spring.annotation.VaadinComponent;

import com.vaadin.annotations.AutoGenerated;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

@VaadinComponent
public class Header extends CustomComponent {

	/*- VaadinEditorProperties={"grid":"RegularGrid,20","showGrid":true,"snapToGrid":true,"snapToObject":true,"movingGuides":false,"snappingDistance":10} */

	@AutoGenerated
	private VerticalLayout mainLayout;
	@AutoGenerated
	private GridLayout modulosLayout;
	@AutoGenerated
	private MenuBar menuBar;
	@AutoGenerated
	private Panel userDetailsPanel;
	@AutoGenerated
	private HorizontalLayout horizontalLayout_2;
	@AutoGenerated
	private ComboBox rolCBox;
	@AutoGenerated
	private Label userIdLabel;
	final private int MODULOS_POR_FILA = 3;
	private static final long serialVersionUID = 1L;

	/**
	 * The constructor should first build the main layout, set the composition
	 * root and then do any custom initialization.
	 *
	 * The constructor will not be automatically regenerated by the visual
	 * editor.
	 */
	public Header() {
		buildMainLayout();
		setCompositionRoot(mainLayout);
	}

	public void renderHeader() {
		Usuario u = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		userIdLabel.setValue(u.getIdUsuario());
		u.getRoles()
			.stream()
			.forEach(rol->{
				rolCBox.addItem(rol.getIdRol());
			});
		
		rolCBox.addValueChangeListener(vchlnrEv->{
			modulosLayout.removeAllComponents();
			modulosLayout.setVisible(true);
			menuBar.setVisible(false);
			modulosLayout.setRows(3);//Rows Iniciales
			modulosLayout.setColumns((MODULOS_POR_FILA*2)+1);
			
			Rol rolSel = u.getRoles()
							.stream()
							.filter(rol-> rol.getIdRol().equals(rolCBox.getValue()))
							.findFirst().get();
			
			int filasModulos= ((filasModulos=rolSel.getModulos().size())%MODULOS_POR_FILA==0)?filasModulos/MODULOS_POR_FILA:(filasModulos/MODULOS_POR_FILA)+1;
			if(filasModulos>1){
				modulosLayout.setRows(3 + (filasModulos*2));
			}
			
			int modulosProcesados=0;
			for (int row = 0; row < filasModulos; row++){
				if(row%2!=0){
					modulosLayout.setRowExpandRatio(row, 2/modulosLayout.getRows());
				}else{
					modulosLayout.setRowExpandRatio(row, 1/modulosLayout.getRows());
				}
				for(int col=0;col<MODULOS_POR_FILA;col++){
					Modulo modulo = rolSel.getModulos().get(modulosProcesados);
					Button button =  new Button(modulo.getDescripcion());
					button.addClickListener(clickEvnt->{
						menuBar.removeItems();
						menuBar.setVisible(true);
						menuBar.setEnabled(true);
						mainLayout.setExpandRatio(userDetailsPanel,0.0f);
						mainLayout.setExpandRatio(menuBar,1.0f);
						mainLayout.setComponentAlignment(menuBar, Alignment.TOP_LEFT);
						modulosLayout.setVisible(false);
						modulo.getFunciones()
									.stream()
									.forEach(funcion->{
										Optional<List<Funcion>> optHijas = Optional.ofNullable(funcion.getHijas());
										MenuItem padre = null;
										if(optHijas.isPresent()){
											padre = menuBar.addItem(funcion.getTitulo(),null);
										}else{
											padre = menuBar.addItem(funcion.getTitulo(), 
													selectedItem->{
														Notification.show(funcion.getDescripcion());
													});	
										}

										padre.setEnabled(true);
										recorreFuncionesHijas(padre,optHijas);
									});
					});
					modulosLayout.addComponent(button, row+1, col+1);
					
					modulosLayout.setComponentAlignment(button, Alignment.TOP_CENTER);
					modulosProcesados++;
					if(modulosProcesados==rolSel.getModulos().size()){
						break;
					}
				}
			}
		});
		
	}

	private void recorreFuncionesHijas(MenuItem padre, Optional<List<Funcion>> optHijas) {
		optHijas.ifPresent(hijas->{
			hijas
				.stream()
				.forEach(funcion->{
					Optional<List<Funcion>> optHijas2 = Optional.ofNullable(funcion.getHijas());
					MenuItem nvoPadre = null;
					if(optHijas2.isPresent()){
						nvoPadre = padre.addItem(funcion.getTitulo(),null);
					}else{
						nvoPadre = padre.addItem(funcion.getTitulo(), 
								selectedItem->{
									Notification.show(funcion.getDescripcion());
								});
					}
					nvoPadre.setEnabled(true);
					recorreFuncionesHijas(nvoPadre,optHijas2);
				});
		});
	}

	@AutoGenerated
	private VerticalLayout buildMainLayout() {
		// common part: create layout
		mainLayout = new VerticalLayout();
		mainLayout.setImmediate(false);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		mainLayout.setMargin(false);
		
		// top-level component properties
		setWidth("100.0%");
		setHeight("100.0%");
		
		// userDetailsPanel
		userDetailsPanel = buildUserDetailsPanel();
		mainLayout.addComponent(userDetailsPanel);
		mainLayout.setComponentAlignment(userDetailsPanel, new Alignment(6));
		
		// menuBar
		menuBar = new MenuBar();
		menuBar.setEnabled(false);
		menuBar.setImmediate(false);
		menuBar.setVisible(false);
		menuBar.setWidth("100.0%");
		menuBar.setHeight("-1px");
		mainLayout.addComponent(menuBar);
		
		// modulosLayout
		modulosLayout = new GridLayout();
		modulosLayout.setImmediate(false);
		modulosLayout.setWidth("-1px");
		modulosLayout.setHeight("100.0%");
		modulosLayout.setMargin(false);
		modulosLayout.setSpacing(true);
		mainLayout.addComponent(modulosLayout);
		mainLayout.setExpandRatio(modulosLayout, 20.0f);
		mainLayout.setComponentAlignment(modulosLayout, new Alignment(48));
		
		return mainLayout;
	}

	@AutoGenerated
	private Panel buildUserDetailsPanel() {
		// common part: create layout
		userDetailsPanel = new Panel();
		userDetailsPanel.setImmediate(false);
		userDetailsPanel.setWidth("400px");
		userDetailsPanel.setHeight("65px");
		
		// horizontalLayout_2
		horizontalLayout_2 = buildHorizontalLayout_2();
		userDetailsPanel.setContent(horizontalLayout_2);
		
		return userDetailsPanel;
	}

	@AutoGenerated
	private HorizontalLayout buildHorizontalLayout_2() {
		// common part: create layout
		horizontalLayout_2 = new HorizontalLayout();
		horizontalLayout_2.setImmediate(false);
		horizontalLayout_2.setWidth("100.0%");
		horizontalLayout_2.setHeight("-1px");
		horizontalLayout_2.setMargin(true);
		
		// userIdLabel
		userIdLabel = new Label();
		userIdLabel.setImmediate(false);
		userIdLabel.setWidth("-1px");
		userIdLabel.setHeight("-1px");
		userIdLabel.setValue("Label");
		horizontalLayout_2.addComponent(userIdLabel);
		horizontalLayout_2.setExpandRatio(userIdLabel, 1.0f);
		horizontalLayout_2.setComponentAlignment(userIdLabel, new Alignment(48));
		
		// rolCBox
		rolCBox = new ComboBox();
		rolCBox.setImmediate(true);
		rolCBox.setWidth("-1px");
		rolCBox.setHeight("-1px");
		horizontalLayout_2.addComponent(rolCBox);
		horizontalLayout_2.setExpandRatio(rolCBox, 2.0f);
		horizontalLayout_2.setComponentAlignment(rolCBox, new Alignment(34));
		
		return horizontalLayout_2;
	}
}