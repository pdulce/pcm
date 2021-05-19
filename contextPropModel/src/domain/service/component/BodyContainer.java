package domain.service.component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import domain.common.InternalErrorsConstants;
import domain.common.PCMConstants;
import domain.common.exceptions.DatabaseException;
import domain.common.exceptions.MessageException;
import domain.common.exceptions.PCMConfigurationException;
import domain.service.DomainService;
import domain.service.component.definitions.FieldViewSet;
import domain.service.component.definitions.FieldViewSetCollection;
import domain.service.component.factory.BodyContainerFactory;
import domain.service.component.factory.IBodyContainer;
import domain.service.dataccess.IDataAccess;
import domain.service.dataccess.dto.Datamap;
import domain.service.dataccess.dto.IFieldValue;
import domain.service.event.AbstractAction;

public class BodyContainer implements IBodyContainer {

	private static final String FORM_ELEMENT = "form", GRID_ELEMENT = "grid";

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

	public BodyContainer(final DomainService domainService, final IDataAccess dataAccess_, final Datamap datamap) 
			throws PCMConfigurationException {
		
		final Collection<Element> viewElements_ = domainService.extractViewComponentElementsByAction(datamap.getEvent());
		if (viewElements_.isEmpty()) {
			throw new PCMConfigurationException("Error: viewcomponent must be defined for this service");
		}
		List<IViewComponent> grids = new ArrayList<IViewComponent>();
		List<IViewComponent> forms = new ArrayList<IViewComponent>();
		Iterator<Element> iteViewComponents = viewElements_.iterator();
		int posicion = 0;
		while (iteViewComponents.hasNext()) {
			Element viewComponentElement = iteViewComponents.next();
			int numberOfForms = viewComponentElement.getElementsByTagName(FORM_ELEMENT).getLength();
			for (int i = 0; i < numberOfForms; i++) {
				final Element elementForm = (Element) viewComponentElement.getElementsByTagName(FORM_ELEMENT).item(i);
				forms.add(posicion++, new Form(elementForm, dataAccess_, datamap));
			}
			int numGrids = viewComponentElement.getElementsByTagName(GRID_ELEMENT).getLength();
			for (int j = 0; j < numGrids; j++) {
				final Element elementGrid = (Element) viewComponentElement.getElementsByTagName(GRID_ELEMENT).item(j);
				grids.add(new PaginationGrid(domainService.getUseCaseName(), elementGrid, ((Form) forms.get(posicion - 1)).getUniqueName(), datamap));
			}
		}
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
				} catch (final Throwable exc) {
					return null;
				}
			}
		}
		return fieldViewSets;
	}

	@Override
	public final IBodyContainer copyOf(final IDataAccess dataAccess) throws PCMConfigurationException, DatabaseException {
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
					colOfForms.add(comp.copyOf(dataAccess));
				}else{
					colOfGrids.add(comp.copyOf(dataAccess));
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
	public String toXML(final Datamap datamap, final IDataAccess dataAccess_, boolean submitted, final List<MessageException> applicationMsgs) {
		final StringBuilder sbXML = new StringBuilder();
		try {	
			if (applicationMsgs != null && !applicationMsgs.isEmpty()) {
				final Iterator<MessageException> iteMsg = applicationMsgs.iterator();
				while (iteMsg.hasNext()) {
					sbXML.append(iteMsg.next().toXML(datamap.getLanguage()));
				}
				sbXML.append("<BR/>");
			}			
			final List<IViewComponent> subcomps = new ArrayList<IViewComponent>();
			if (getGrids().isEmpty()) {
				subcomps.addAll(getForms());
				for (final IViewComponent form : subcomps) {										
					sbXML.append(form.toXHTML(datamap, dataAccess_, submitted));
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
							sbXML.append(formOfContainer.toXHTML(datamap, dataAccess_, submitted));
							break;
						}
					}
					boolean isQueryEvent = false;
					String eventLast = datamap.getParameter(PCMConstants.EVENT);
					if (eventLast.split(PCMConstants.REGEXP_POINT).length > 1){
						eventLast = eventLast.split(PCMConstants.REGEXP_POINT)[1];
						isQueryEvent = AbstractAction.isQueryEvent(eventLast);
					}
					if (((PaginationGrid) paginationGrid).getMasterNamespace() == null
							|| "".equals(((PaginationGrid) paginationGrid).getMasterNamespace()) && isQueryEvent) {//grid a pintar de un escenario Form&GRID de query event
						sbXML.append(paginationGrid.toXHTML(datamap, dataAccess_, submitted));
					}
				}
			}
		} catch (final Throwable exc) {
			AbstractComponent.log.log(Level.SEVERE, InternalErrorsConstants.BODY_CREATING_EXCEPTION, exc);
		}
		return sbXML.toString();
	}
	
	
	public static final IBodyContainer getContainerOfView(final Datamap datamap, final IDataAccess dataAccess_,
			final DomainService domainService) throws PCMConfigurationException, DatabaseException {		
		try {			
			return BodyContainerFactory.getFactoryInstance().getViewComponent(dataAccess_, datamap, domainService);
		} catch (PCMConfigurationException e) {
			throw new RuntimeException("Error getting org.w3c.Element, CU: " + 
						domainService.getUseCaseName() + " and EVENT: " + datamap.getEvent());
		}
	}
	
}
