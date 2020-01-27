package pcm.context.viewmodel.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import pcm.common.InternalErrorsConstants;
import pcm.common.PCMConstants;
import pcm.common.exceptions.DatabaseException;
import pcm.common.exceptions.MessageException;
import pcm.common.exceptions.PCMConfigurationException;
import pcm.common.utils.CommonUtils;
import pcm.comunication.actions.Event;
import pcm.comunication.dispatcher.BasePCMServlet;
import pcm.comunication.dispatcher.ContextApp;
import pcm.comunication.dispatcher.RequestWrapper;
import pcm.context.data.IFieldValue;
import pcm.context.logicmodel.IDataAccess;
import pcm.context.viewmodel.IViewMetamodel;
import pcm.context.viewmodel.ViewMetamodelFactory;
import pcm.context.viewmodel.definitions.FieldViewSet;
import pcm.context.viewmodel.definitions.FieldViewSetCollection;
import pcm.context.viewmodel.factory.BodyContainerFactory;
import pcm.context.viewmodel.factory.IBodyContainer;

public class BodyContainer implements IBodyContainer {

	//private String event_;

	private Map<String, Collection<IViewComponent>> subcomponents;

	private BodyContainer() {
		// constructor no visible al exterior
	}
	
	public final String getName() {
		return PCMConstants.EMPTY_;
	}

	private final Map<String, Collection<IViewComponent>> getSubComponents() {
		if (this.subcomponents == null) {
			this.subcomponents = new HashMap<String, Collection<IViewComponent>>();
		}
		return this.subcomponents;
	}

	@Override
	public final void setFieldViewSetCriteria(List<FieldViewSetCollection> col) {
		final Iterator<IViewComponent> iteForms = getForms().iterator();
		while (iteForms.hasNext()) {
			IViewComponent formIesimo = iteForms.next();
			formIesimo.updateModelEntities(col);
		}
	}

	@Override
	public final void refresh(final IViewComponent cloneCmp, final HashMap<String, IFieldValue> values) {
		final Iterator<Collection<IViewComponent>> ite = this.getSubComponents().values().iterator();
		while (ite.hasNext()) {
			final Collection<IViewComponent> components = ite.next();
			Iterator<IViewComponent> iteComponents = components.iterator();
			while (iteComponents.hasNext()) {
				IViewComponent comp = iteComponents.next();
				if (comp.getName().equals(cloneCmp.getName())) {
					comp.refreshValues(values);
					break;
				}
			}
		}
		this.getSubComponents().values();
	}

	public BodyContainer(String service, final Collection<Element> viewElements_, final IDataAccess dataAccess_, final ContextApp appCtx,
			final RequestWrapper request, final String event_) throws PCMConfigurationException {
		//this.event = event_;

		if (viewElements_.isEmpty()) {
			throw new PCMConfigurationException("Error: viewcomponent must be defined for this service");
		}
		List<IViewComponent> grids = new ArrayList<IViewComponent>();
		List<IViewComponent> forms = new ArrayList<IViewComponent>();
		Iterator<Element> iteViewComponents = viewElements_.iterator();
		int posicion = 0;
		while (iteViewComponents.hasNext()) {
			Element viewComponentElement = iteViewComponents.next();
			int numberOfForms = viewComponentElement.getElementsByTagName(IViewMetamodel.FORM_ELEMENT).getLength();
			for (int i = 0; i < numberOfForms; i++) {
				final Element elementForm = (Element) viewComponentElement.getElementsByTagName(IViewMetamodel.FORM_ELEMENT).item(i);
				forms.add(posicion++, new Form(service, event_, elementForm, dataAccess_, appCtx, request));
			}

			int numGrids = viewComponentElement.getElementsByTagName(IViewMetamodel.GRID_ELEMENT).getLength();
			for (int j = 0; j < numGrids; j++) {
				final Element elementGrid = (Element) viewComponentElement.getElementsByTagName(IViewMetamodel.GRID_ELEMENT).item(j);
				/** engarzo con el oltimo form de este view component ***/
				grids.add(new PaginationGrid(service, elementGrid, ((Form) forms.get(posicion - 1)).getUniqueName(), appCtx, request));
			}

		}// while viewcomponents

		this.getSubComponents().put(IViewComponent.FORM_TYPE, forms);
		this.getSubComponents().put(IViewComponent.GRID_TYPE, grids);
	}

	private final boolean existsInCollection(final Collection<FieldViewSet> collectionOfFieldSet, final FieldViewSet fieldSet) {
		final Iterator<FieldViewSet> iteFieldSet = collectionOfFieldSet.iterator();
		while (iteFieldSet.hasNext()) {
			final FieldViewSet fieldSet_ = iteFieldSet.next();
			if (fieldSet.getContextName().equals(fieldSet_.getContextName())
					&& fieldSet.getEntityDef().getName().equals(fieldSet_.getEntityDef().getName())) {
				return true;
			}
		}
		return false;
	}

	public final Collection<FieldViewSet> getFieldViewSets() {
		return null;
	}

	public final Collection<FieldViewSetCollection> getFieldViewSetCollection() {
		final Collection<FieldViewSetCollection> fieldViewSets = new ArrayList<FieldViewSetCollection>();
		final Iterator<Collection<IViewComponent>> iteSubcomponents = this.getSubComponents().values().iterator();
		while (iteSubcomponents.hasNext()) {
			Iterator<IViewComponent> iteSubcomp = iteSubcomponents.next().iterator();
			while (iteSubcomp.hasNext()) {
				final Iterator<FieldViewSet> iteFieldView = iteSubcomp.next().getFieldViewSets().iterator();
				final Collection<FieldViewSet> fieldSets = new ArrayList<FieldViewSet>();
				String dict = null;
				while (iteFieldView.hasNext()) {
					final FieldViewSet fieldViewSet = iteFieldView.next();
					if (fieldViewSet != null && !this.existsInCollection(fieldSets, fieldViewSet)) {
						fieldSets.add(fieldViewSet);
						if (dict == null) {
							dict = fieldViewSet.getDictionaryName();
						}
					}
				}
				try {
					final FieldViewSetCollection fieldViewSetCollection = new FieldViewSetCollection(fieldSets);
					fieldViewSets.add(fieldViewSetCollection);
				}
				catch (final Throwable exc) {
					return null;
				}
			}
		}

		return fieldViewSets;
	}

	@Override
	public final IBodyContainer copyOf() throws PCMConfigurationException, DatabaseException {
		final BodyContainer newV = new BodyContainer();
		Collection<IViewComponent> colOfForms = new ArrayList<IViewComponent>();
		Collection<IViewComponent> colOfGrids = new ArrayList<IViewComponent>();
		newV.subcomponents = new HashMap<String, Collection<IViewComponent>>();
		final Iterator<Collection<IViewComponent>> iteComponentes = this.getSubComponents().values().iterator();
		while (iteComponentes.hasNext()) {
			Iterator<IViewComponent> ite = iteComponentes.next().iterator();
			while (ite.hasNext()) {
				final IViewComponent comp = ite.next();
				if (comp.isForm()){
					colOfForms.add(comp.copyOf());
				}else{
					colOfGrids.add(comp.copyOf());
				}
			}
		}
		newV.subcomponents.put(IViewComponent.FORM_TYPE, colOfForms);
		newV.subcomponents.put(IViewComponent.GRID_TYPE, colOfGrids);
		return newV;
	}

	@Override
	public final List<IViewComponent> getForms() {
		final List<IViewComponent> forms = new ArrayList<IViewComponent>();
		final Iterator<Collection<IViewComponent>> iteComponentes = this.getSubComponents().values().iterator();
		while (iteComponentes.hasNext()) {
			Iterator<IViewComponent> ite = iteComponentes.next().iterator();
			while (ite.hasNext()) {
				final IViewComponent comp = ite.next();
				if (comp.isForm()) {
					forms.add(comp);
				}
			}
		}
		return forms;
	}

	@Override
	public final List<IViewComponent> getGrids() {
		final List<IViewComponent> grids = new ArrayList<IViewComponent>();
		final Iterator<Collection<IViewComponent>> iteComponentes = this.getSubComponents().values().iterator();
		while (iteComponentes.hasNext()) {
			Iterator<IViewComponent> ite = iteComponentes.next().iterator();
			while (ite.hasNext()) {
				final IViewComponent comp = ite.next();
				if (comp.isGrid()) {
					grids.add(comp);
				}
			}
		}
		return grids;
	}

	public final IFieldValue getValueOfField(final String qualifiedName) {
		final Iterator<IViewComponent> iteForms = getForms().iterator();
		IFieldValue fieldValue = null;
		while (iteForms.hasNext()) {
			IViewComponent formIesimo = iteForms.next();
			fieldValue = formIesimo.getValueOfField(qualifiedName);
		}
		return fieldValue;
	}

	@Override
	public String toXML(final RequestWrapper request, final IDataAccess dataAccess_, boolean submitted, final List<MessageException> applicationMsgs) {
		final StringBuilder sbXML = new StringBuilder();
		try {
			//en primer lugar, los mensajes y errores de aplicacion			
			if (applicationMsgs != null && !applicationMsgs.isEmpty()) {
				final Iterator<MessageException> iteMsg = applicationMsgs.iterator();
				while (iteMsg.hasNext()) {
					sbXML.append(iteMsg.next().toXML(CommonUtils.getLanguage(request)));
				}
				sbXML.append("<BR/>");
			}			
			//en primer lugar, los componentes
			final List<IViewComponent> subcomps = new ArrayList<IViewComponent>();
			if (getGrids().isEmpty()) {
				
				subcomps.addAll(getForms());
				for (final IViewComponent form : subcomps) {										
					sbXML.append(form.toXHTML(request, dataAccess_, submitted));
				}
				
			} else {
				
				List<String> idFormsPainted = new ArrayList<String>();
				subcomps.addAll(getGrids());
				for (final IViewComponent paginationGrid : subcomps) {
					String idOfForm = ((PaginationGrid) paginationGrid).getMyFormIdentifier();
					Iterator<IViewComponent> iteForms = getForms().iterator();
					while (iteForms.hasNext()) {
						Form formOfContainer = (Form) iteForms.next();
						if (formOfContainer.getUniqueName().equals(idOfForm) && !idFormsPainted.contains(idOfForm)) {
							idFormsPainted.add(idOfForm);
							sbXML.append(formOfContainer.toXHTML(request, dataAccess_, submitted));
							break;
						}
					}
					
					boolean isQueryEvent = false;
					String eventLast = request.getParameter(PCMConstants.EVENT);
					if (eventLast.split(PCMConstants.REGEXP_POINT).length > 1){
						eventLast = eventLast.split(PCMConstants.REGEXP_POINT)[1];
						isQueryEvent = Event.isQueryEvent(eventLast);
					}
					if (((PaginationGrid) paginationGrid).getMasterNamespace() == null
							|| "".equals(((PaginationGrid) paginationGrid).getMasterNamespace()) && isQueryEvent) {//grid a pintar de un escenario Form&GRID de query event
						sbXML.append(paginationGrid.toXHTML(request, dataAccess_, submitted));
					}
				}
			}

		}
		catch (final Throwable exc) {
			BasePCMServlet.log.log(Level.SEVERE, InternalErrorsConstants.BODY_CREATING_EXCEPTION, exc);
		}
		return sbXML.toString();
	}
	
	
	public static final IBodyContainer getContainerOfView(final RequestWrapper requestWrapper, final IDataAccess dataAccess_,
			final Element actionElement, final ContextApp context, final String event) throws PCMConfigurationException, DatabaseException {
		final Collection<Element> viewComponentNodes = ViewMetamodelFactory.getFactoryInstance().extractViewComponentElementsByAction(
				actionElement);
		return BodyContainerFactory.getFactoryInstance().getViewComponent(viewComponentNodes, dataAccess_, context,	requestWrapper, event);
	}
	
	

}
