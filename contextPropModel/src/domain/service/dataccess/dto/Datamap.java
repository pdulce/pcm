package domain.service.dataccess.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import domain.common.PCMConstants;


public class Datamap {
	
	public static final String USU_ALTA = "USU_A", USU_MOD = "USU_M",	USU_BAJA = "USU_B", 
			FEC_ALTA = "FEC_A", FEC_MOD = "FEC_M", FEC_BAJA = "FEC_B", AUDITFIELDSET_ELEMENT = "auditFieldSet";
	
	private Map<String, List<Object>> data;
	private Collection<String> attributeNames = new ArrayList<>();
	private Collection<String> parameterNames = new ArrayList<>();
	private Collection<String> appProfileSet = new ArrayList<>();
	private String service, uri;
	private String event;
	private String language;
	private String entitiesDictionary;
	private int pageSize;
	
	
	public Datamap(final String entitiesDictionary_, final String uri_, final int pageSize){		
		this.data = new HashMap<String, List<Object>>();
		this.setPageSize(pageSize);
		this.setAttribute(PCMConstants.LANGUAGE, PCMConstants.DEFAULT_LANG);
		this.language = PCMConstants.DEFAULT_LANG;
		this.entitiesDictionary = entitiesDictionary_;
		this.setAttribute(PCMConstants.APP_DICTIONARY, entitiesDictionary_);
		this.uri = uri_;
		this.setAttribute(PCMConstants.APPURI_, uri_);
	}
	
	public void copyMap(final Datamap toCopy) {
		Collection<String> attrsKeySet = toCopy.getAttributeNames();
		Iterator<String> attrsKeysIte = attrsKeySet.iterator();
		while (attrsKeysIte.hasNext()) {
			String attrKey = attrsKeysIte.next();
			Object attrValue = toCopy.getAttribute(attrKey);
			this.setAttribute(attrKey, attrValue);
		}
		
		Collection<String> paramKeySet = toCopy.getParameterNames();
		Iterator<String> paramsKeysIte = paramKeySet.iterator();
		while (paramsKeysIte.hasNext()) {
			String paramKey = paramsKeysIte.next();
			String[] paramValues = toCopy.getParameterValues(paramKey);
			List<String> arr = new ArrayList<String>();
			for (int i=0;i<paramValues.length;i++) {
				arr.add(paramValues[i]);
			}
			this.setParameterValues(paramKey, arr);
		}
		
	}
	
	public  Map<String, List<Object>> getAllDataMap(){
		return this.data;
	}
	
	public Collection<String> getAppProfileSet() {
		return this.appProfileSet;
	}

	public void setAppProfileSet(Collection<String> appProfileSet_) {
		this.appProfileSet = appProfileSet_;
	}

	public String getService() {
		return this.service;
	}

	public void setService(String service_) {
		this.service = service_;
	}

	public String getEvent() {
		return this.event;
	}
	
	public String getUri() {
		return this.uri;
	}


	public void setEvent(String event_) {
		this.event = event_;
		List<String> val_List = new ArrayList<String>();
		val_List.add(getService().concat(".").concat(event_));
		this.setParameterValues(PCMConstants.EVENT, val_List);
	}
		
	public void setPageSize(final int page_size){
		this.pageSize = page_size;
	}
	
	public int getPageSize(){
		return this.pageSize;
	}
	
	public String getAppProfile() {
		if (this.getAttribute(PCMConstants.APP_PROFILE) == null) {
			return "non-profile";
		}
		return (String) this.getAttribute(PCMConstants.APP_PROFILE);
	}
	
	public Collection<String> getAttributeNames(){
		return this.attributeNames;		
	}
	
	public Collection<String> getParameterNames(){
		return this.parameterNames;		
	}
	
	private Map<String, List<String>> getParameters(){
		Map<String, List<String>> lista = new HashMap<String, List<String>>();
		for (String param: this.parameterNames){
			List<String> values = new ArrayList<String>();
			for (Object valObject: this.data.get(param)){
				values.add((String)valObject);
			}
			lista.put(param, values);
		}
		return lista;
	}
	
	public String[] getParameterValues(final String param){
		Map<String, List<String>> lista = getParameters();
		if (lista.containsKey(param)){
			List<String> listaValues = lista.get(param);
			String[] arr = new String[listaValues.size()];
			int i = 0;
			for (final String val:listaValues){
				arr[i++] = val;
			}
			return arr;
		}
		return new String[]{};
		
	}
	public String getEntitiesDictionary(){
		return this.entitiesDictionary;
	}
	
	public void setEntitiesDictionary(String ent_){
		this.entitiesDictionary = ent_;		
	}
	
	public Object getAttribute(final String attr){
		if (this.data.get(attr) == null){
			return null;
		}
		return this.data.get(attr).get(0);
	}
	
	public void setAttribute(final String param, final Object data){
		List<Object> dataValues = null;
		if (this.data.containsKey(param)){
			dataValues = this.data.get(param);
			dataValues.clear();
		}else{
			dataValues = new ArrayList<Object>();
			this.data.put(param, dataValues);
		}
		dataValues.add(data);
		if (param.equals(PCMConstants.APP_DICTIONARY)){
			setEntitiesDictionary(data.toString());	
		}
		this.attributeNames.add(param);
	}
	
	public void removeAttribute(final String attr){
		if (this.attributeNames == null || this.attributeNames.isEmpty()) {
			return;
		}
		this.attributeNames.remove(attr);
		this.data.remove(attr);
	}
	
	public void removeParameter(final String param){
		if (this.parameterNames == null || this.parameterNames.isEmpty()) {
			return;
		}
			
		this.parameterNames.remove(param);
		this.data.remove(param);
	}
	
	public String getParameter(final String param){
		if (this.data.get(param) == null){
			return null;
		}
		return (String)(this.data.get(param).get(0));
	}
	
	public void setParameter(final String param, String value){
		List<String> values = new ArrayList<String>();
		values.add(value);
		setParameterValues(param, values);
	}
	
	public void setParameterValues(final String param, final List<String> values){
		List<Object> dataValues = null;
		if (this.data.containsKey(param)){
			dataValues = this.data.get(param);
		}else{
			dataValues = new ArrayList<Object>();
			dataValues.addAll(values);
		}
		this.data.put(param, dataValues);
		
		if (param.equals(PCMConstants.APP_DICTIONARY)){
			setEntitiesDictionary(values.get(0));	
		}
		this.parameterNames.add(param);
	}
	
	public void setParameterValues(final String param, final String[] values){
		List<Object> dataValues = null;
		if (this.data.containsKey(param)){
			dataValues = this.data.get(param);
		}else{
			dataValues = new ArrayList<Object>();
			for (int i=0;i<values.length;i++) {
				dataValues.add(values[i]);
			}
		}
		this.data.put(param, dataValues);
		
		if (param.equals(PCMConstants.APP_DICTIONARY)){
			setEntitiesDictionary(values[0]);
		}
		this.parameterNames.add(param);
	}

	
	public void setLanguage(final String lang){
		this.language = lang;
	}
	
	public String getLanguage(){
		return this.language;
	}
	
}
